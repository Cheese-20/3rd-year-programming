package com.example.as1t2.model;

public class Player {
    public String name;
    public Token token;

    public Player(String name, Token token){
        this.name = name;
        this.token = token;

    }

    public String getName(){
        return name;
    }

    public Token getToken(){
        return token;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
