package nmu.wrpv.pubsub;

@FunctionalInterface
public interface MessageListener {
    void onMessageReceived(String topic, int publisherId, String message);
}