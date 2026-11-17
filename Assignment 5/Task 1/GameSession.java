package nmu.wrpv.pubsub;
import java.util.List;

public class GameSession implements Runnable {
    private final int gameId;
    private final List<Integer> players;
    private final NetworkPubSubClient broker;
    private final String gameTopic;
    private int currentTurnIndex = 0;
    private volatile boolean isRunning = true;

    public GameSession(int gameId, List<Integer> players, NetworkPubSubClient broker) {
        this.gameId = gameId;
        this.players = players;
        this.broker = broker;
        this.gameTopic = "game_" + gameId;
    }

    @Override
    public void run() {
        // Subscribe the server session to this specific game topic
        broker.subscribe(gameTopic, (topic, senderId, message) -> {
            processGameMessage(senderId, message);
        });

        // Notify each player which game they joined and what their turn/token index is (0 to N-1)
        for (int i = 0; i < players.size(); i++) {
            int pId = players.get(i);
            // Send direct assignment via the lobby topic
            broker.publish(SequenceGameServer.LOBBY_TOPIC, "ASSIGN:" + pId + ":" + gameId + ":" + i + ":" + players.size());
        }

        // Broadcast initial START message to the game topic
        try {
            Thread.sleep(500); // Small delay to let clients subscribe to gameTopic
        } catch (InterruptedException ignored) {}

        broker.publish(gameTopic, "START:" + gameId + ":" + players.get(0));
        System.out.println("[SESSION " + gameId + "] Started. Player " + players.get(0) + " takes first turn.");

        // Monitor session lifecycle
        while (isRunning) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }

        broker.unsubscribe(gameTopic);
        System.out.println("[SESSION " + gameId + "] Terminated.");
    }

    private void processGameMessage(int senderId, String message) {
        String[] parts = message.split(":");
        String cmd = parts[0];

        switch (cmd) {
            case "ROLL":
                // Broadcast dice roll to all clients
                // Format: ROLL:die1:die2:total
                broker.publish(gameTopic, "ROLLED:" + senderId + ":" + parts[1] + ":" + parts[2] + ":" + parts[3]);
                break;

            case "MOVE":
                // Format: MOVE:row:col
                System.out.println("[SESSION " + gameId + "] Player " + senderId + " played move: " + message);
                // Broadcast the move to everyone so their local models stay in sync
                broker.publish(gameTopic, "MOVE:" + senderId + ":" + parts[1] + ":" + parts[2]);
                
                // Advance turn to next player and broadcast
                currentTurnIndex = (currentTurnIndex + 1) % players.size();
                broker.publish(gameTopic, "TURN:" + players.get(currentTurnIndex));
                break;

            case "WIN":
                System.out.println("[SESSION " + gameId + "] Player " + senderId + " won the game!");
                broker.publish(gameTopic, "GAMEOVER:" + senderId);
                isRunning = false;
                break;

            case "LEAVE":
                System.out.println("[SESSION " + gameId + "] Player " + senderId + " disconnected.");
                broker.publish(gameTopic, "PLAYER_DISCONNECTED:" + senderId);
                isRunning = false;
                break;
        }
    }
}