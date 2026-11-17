package com.example.assignment3;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Transaction <T>{
    String accountName;
    String transactionType;
    Date transactionDate;
    String transactionDescription;
    Double transactionAmount;
    //Where multiple tags will be stored.
    List<String> tags = new ArrayList<>();

    public Transaction(String accountName, String transactionType, Date transactionDate, String transactionDescription, Double transactionAmount) {
        this.accountName = accountName;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.transactionDescription = transactionDescription;
        this.transactionAmount = transactionAmount;
    }

    public Transaction(){}

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public Double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag){
        tags.add(tag);
    }
}
