package com.example.assignment2.modules;

public class Message<T> {
     String publisherId;
     String topic;
     T content;

    public String getPublisherId() {
        return publisherId;
    }

    public Message(String topic, String publisherId, T content) {
        this.topic = topic;
        this.publisherId = publisherId;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "publisherId='" + publisherId + '\'' +
                ", topic='" + topic + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

}
