package com.example.assignment2;

import java.io.Serializable;
import java.util.UUID;

public class Contact implements Serializable {
    private String id;
    private String name;
    private String number;
    private int avatarResId;

    public Contact(String name, String number, int avatarResId) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.number = number;
        this.avatarResId = avatarResId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public int getAvatarResId() { return avatarResId; }
    public void setAvatarResId(int avatarResId) { this.avatarResId = avatarResId; }
}