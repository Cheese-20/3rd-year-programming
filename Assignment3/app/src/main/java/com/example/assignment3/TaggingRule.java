package com.example.assignment3;

import java.util.List;

//for dynamic tagging
//Map keyword to one or more tags
public class TaggingRule {
    String keyword;
    List<String> tags;
    public TaggingRule(String keyword, List<String> tags) {
        this.keyword = keyword;
        this.tags = tags;
    }
    public String getKeyword() {
        return keyword;
    }
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    public List<String> getTags() {
        return tags;
    }
    public void addTag(String tag){
        tags.add(tag);
    }
}
