package nmu.wrpv.pubsub;

public class Main {
    public static void main(String[] args) {
        try {
            // Client 1
            NetworkPubSubClient client1 = new NetworkPubSubClient();
            client1.connect("localhost", 5555);
            System.out.println("Client 1 initialized with ID: " + client1.getClientId());

            // Client 2
            NetworkPubSubClient client2 = new NetworkPubSubClient();
            client2.connect("localhost", 5555);
            System.out.println("Client 2 initialized with ID: " + client2.getClientId());

            // Client 1 subscribes to "game-events" and "chat"
            client1.subscribe("game-events", (topic, publisherId, msg) -> {
                System.out.println("[Client 1 Received] on " + topic + " from ID " + publisherId + ": " + msg);
            });

            // Client 2 subscribes to "game-events"
            client2.subscribe("game-events", (topic, publisherId, msg) -> {
                System.out.println("[Client 2 Received] on " + topic + " from ID " + publisherId + ": " + msg);
            });

            Thread.sleep(500); // Allow registration

            // Client 1 publishes to "game-events" (Verifies Client 1 receives its own message)
            System.out.println("\n--- Client 1 publishing to 'game-events' ---");
            client1.publish("game-events", "Game Started! Round 1");

            Thread.sleep(500);

            // Client 2 publishes to "game-events"
            System.out.println("\n--- Client 2 publishing to 'game-events' ---");
            client2.publish("game-events", "RollDice: Result=8");

            Thread.sleep(1000);

            client1.disconnect();
            client2.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}