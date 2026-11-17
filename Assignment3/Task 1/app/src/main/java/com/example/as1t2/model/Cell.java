package com.example.as1t2.model;

public class Cell {

    int row;
    int col;
    int value;
    Token token;

    public Cell(int row, int col, int value){
        this.row = row;
        this.col = col;
        this.value = value;
        this.token = Token.NONE;

    }

    public int getRow(){
            return row;
    }

    public int getCol(){
        return col;
    }

    public int getValue() {
        return value;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token){
        this.token = token;
    }

    public boolean isEmpty(){
        return token == Token.NONE;
    }

    public boolean isCorner() {
        return this.value == 2 || this.value == 12;
    }
}
