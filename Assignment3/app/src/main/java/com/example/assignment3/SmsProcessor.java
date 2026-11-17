package com.example.assignment3;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

public class SmsProcessor {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

    public SmsProcessor(Context context, TransactionManager transactionManager) {
        processMessages(context, transactionManager);
    }

    public void processMessages(Context context, TransactionManager manager) {
        Uri smsUri = Telephony.Sms.Inbox.CONTENT_URI;
        String[] projection = new String[]{Telephony.Sms.Inbox.BODY};

        try (Cursor cursor = context.getContentResolver().query(smsUri, projection, null, null, null)) {
            if (cursor == null) return;
            int bodyIndex = cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.BODY);

            while (cursor.moveToNext()) {
                String textBody = cursor.getString(bodyIndex);

                manager.getBankPatterns().stream()
                        .filter(bp -> {
                            Matcher m = bp.getCompiledPattern().matcher(textBody);
                            return m.find();
                        })
                        .findFirst()
                        .ifPresent(bp -> {
                            Matcher matcher = bp.getCompiledPattern().matcher(textBody);
                            if (matcher.find()) {
                                try {
                                    Transaction transaction = new Transaction();
                                    
                                    Map<String, Integer> mapping = bp.getFieldMapping();
                                    
                                    if (mapping.containsKey("account")) {
                                        transaction.setAccountName(matcher.group(mapping.get("account")));
                                    }
                                    if (mapping.containsKey("type")) {
                                        transaction.setTransactionType(matcher.group(mapping.get("type")));
                                    }
                                    if (mapping.containsKey("date")) {
                                        try {
                                            transaction.setTransactionDate(dateFormat.parse(matcher.group(mapping.get("date"))));
                                        } catch (Exception e) {
                                            transaction.setTransactionDate(new Date());
                                        }
                                    }
                                    if (mapping.containsKey("description")) {
                                        transaction.setTransactionDescription(matcher.group(mapping.get("description")));
                                    }
                                    if (mapping.containsKey("amount")) {
                                        String amountStr = matcher.group(mapping.get("amount")).replaceAll("[^\\d.-]", "");
                                        transaction.setTransactionAmount(Double.parseDouble(amountStr));
                                    }

                                    applyTags(transaction, manager);
                                    manager.addTransaction(transaction);
                                } catch (Exception e) {
                                    Log.e("SmsProcessor", "Parsing error: " + e.getMessage());
                                }
                            }
                        });
            }
        }
    }

    private void applyTags(Transaction transaction, TransactionManager manager) {
        String desc = transaction.getTransactionDescription();
        if (desc == null) return;
        
        String lowerDesc = desc.toLowerCase();
        manager.getTaggingRules().stream()
                .filter(rule -> lowerDesc.contains(rule.getKeyword().toLowerCase()))
                .forEach(rule -> rule.getTags().forEach(transaction::addTag));
        
        // Always add basic tags based on amount/type if not already present
        if (transaction.getTransactionAmount() != null) {
            if (transaction.getTransactionAmount() < 0 || 
                (transaction.getTransactionType() != null && transaction.getTransactionType().toLowerCase().contains("pur"))) {
                if (!transaction.getTags().contains("Expense")) transaction.addTag("Expense");
            } else {
                if (!transaction.getTags().contains("Income")) transaction.addTag("Income");
            }
        }
    }
}
