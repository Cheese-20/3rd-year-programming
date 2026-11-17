package com.example.as1t2.model;

import java.util.Random;

public class Dice {
    Random random = new Random();
    int Die1;
    int Die2;

    public void roll(){
        Die1 = random.nextInt(6)+ 1;
        Die2 = random.nextInt(6)+ 1;
    }

    public void setValues(int d1, int d2) {
        this.Die1 = d1;
        this.Die2 = d2;
    }

    public int getDie1() {
        return Die1;
    }
    public int getDie2(){
        return Die2;
    }

    public int getTotal(){
        return Die1 + Die2;
    }
}
