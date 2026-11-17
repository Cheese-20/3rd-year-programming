package com.example.assignment3;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionManager {
    private List<Transaction> transactions = new ArrayList<>();
    private List<BankPattern> bankPatterns = new ArrayList<>();
    private List<TaggingRule> taggingRules = new ArrayList<>();
    private List<List<String>> aggregateFilters = new ArrayList<>();

    public TransactionManager() {
        // Default aggregation groups as requested by the task examples
        aggregateFilters.add(new ArrayList<>(List.of("Expense")));
        aggregateFilters.add(new ArrayList<>(List.of("Income")));
        aggregateFilters.add(new ArrayList<>(List.of("Food", "Expense")));
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void addBankPattern(BankPattern bankPattern) {
        bankPatterns.add(bankPattern);
    }

    public void addTaggingRule(TaggingRule taggingRule) {
        taggingRules.add(taggingRule);
    }

    public void addAggregateFilter(List<String> tags) {
        aggregateFilters.add(tags);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<TaggingRule> getTaggingRules() {
        return taggingRules;
    }

    public List<BankPattern> getBankPatterns() {
        return bankPatterns;
    }

    public List<List<String>> getAggregateFilters() {
        return aggregateFilters;
    }

    public void clearTransactions() {
        transactions.clear();
    }

    /**
     * Requirement: Aggregates using a date range and a collection of tags.
     * All transactions that occurred within the date range and contain ALL the tags.
     */
    public List<Transaction> getFilteredTransactions(Date startDate, Date endDate, List<String> requiredTags) {
        return transactions.stream()
                .filter(t -> (startDate == null || !t.getTransactionDate().before(startDate)) && 
                            (endDate == null || !t.getTransactionDate().after(endDate)))
                .filter(t -> requiredTags == null || requiredTags.isEmpty() || t.getTags().containsAll(requiredTags))
                .collect(Collectors.toList());
    }

    /**
     * Requirement: Aggregate values are totalled and displayed.
     * Calculates the total for a specific list of transactions.
     */
    public double calculateTotal(List<Transaction> filtered) {
        return filtered.stream()
                .mapToDouble(Transaction::getTransactionAmount)
                .sum();
    }

    /**
     * Returns a map of Tag-Collection -> Total Amount for the recycler list.
     * Uses Fluid API (Streams).
     */
    public Map<List<String>, Double> getAggregatedTotals(Date startDate, Date endDate) {
        return aggregateFilters.stream()
                .collect(Collectors.toMap(
                        filter -> filter,
                        filter -> calculateTotal(getFilteredTransactions(startDate, endDate, filter))
                ));
    }
}
