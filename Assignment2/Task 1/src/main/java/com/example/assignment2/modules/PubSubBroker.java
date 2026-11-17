package com.example.assignment2.modules;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PubSubBroker {
    private static PubSubBroker instance;

    // Maps topics to their list of subscribers
    private final Map<String, List<Subscriber<?>>> topicSubscribers;

    private PubSubBroker() {
        // Using thread-safe collections to ensure reliable registration/deregistration at any time
        topicSubscribers = new ConcurrentHashMap<>();
    }

    public static synchronized PubSubBroker getInstance() {
        if (instance == null) {
            instance = new PubSubBroker();
        }
        return instance;
    }

    public <T> void subscribe(String topic, Subscriber<T> subscriber) {
        topicSubscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(subscriber);
    }

    public <T> void unsubscribe(String topic, Subscriber<T> subscriber) {
        List<Subscriber<?>> subscribers = topicSubscribers.get(topic);
        if (subscribers != null) {
            subscribers.remove(subscriber);
        }
    }

    public <T> void publish(Message<T> message) {
        List<Subscriber<?>> subscribers = topicSubscribers.get(message.getTopic());
        if (subscribers != null) {
            for (Subscriber<?> subscriber : subscribers) {
                // Broker forwards the message
                ((Subscriber<T>) subscriber).onMessage(message);
            }
        }
    }
}
