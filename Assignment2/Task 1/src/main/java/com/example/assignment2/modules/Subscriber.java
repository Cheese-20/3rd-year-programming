package com.example.assignment2.modules;
@FunctionalInterface
public interface Subscriber<T> {
    void onMessage(Message<T> message);
}