package com.example.assignment3;

import java.util.Map;
import java.util.regex.Pattern;

public class BankPattern {
    private String bankName;
    private String regex;
    private Pattern compiledPattern;
    // Maps field name to capture group index
    private Map<String, Integer> fieldMapping;

    public BankPattern(String bankName, String regex, Map<String, Integer> fieldMapping) {
        this.bankName = bankName;
        this.regex = regex;
        this.fieldMapping = fieldMapping;
        this.compiledPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }

    public String getBankName() { return bankName; }
    public String getRegex() { return regex; }
    public Pattern getCompiledPattern() { return compiledPattern; }
    public Map<String, Integer> getFieldMapping() { return fieldMapping; }
    
    public Integer getGroupForField(String fieldName) {
        return fieldMapping.get(fieldName);
    }
}
