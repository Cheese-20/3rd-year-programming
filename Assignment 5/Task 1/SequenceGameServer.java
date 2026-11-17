package nmu.wrpv.pubsub;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class SequenceGameServer {
    private static final String BROKER_HOST = "localhost";
    private static final int BROKER_PORT = 5555;
    private static final String LOBBY_TOPIC = "lobby";

    private final NetworkPubSubClient serverClient;
    private final Queue<Integer> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final Random random = new Random();
    private int nextGameId = 1;

    public SequenceGameServer() {
        this.serverClient = new NetworkPubSubClient();
    }

    public void start() throws IOException {
        serverClient.connect(BROKER_HOST, BROKER_PORT);
        System.out.println("[GAME SERVER] Connected to Broker. Listening on topic: " + LOBBY_TOPIC);

        // Listen for player requests to join a game
        serverClient.subscribe(LOBBY_TOPIC, (topic, senderId, msg) -> {
            if ("JOIN".equalsIgnoreCase(msg.trim())) {
                handlePlayerJoin(senderId);
            }
        });
    }

    private synchronized void handlePlayerJoin(int playerId) {
        if (!waitingPlayers.contains(playerId)) {
            waitingPlayers.add(playerId);
            System.out.println("[GAME SERVER] Player " + playerId + " added to queue. In queue: " + waitingPlayers.size());
            checkAndFormGame();
        }
    }

    private void checkAndFormGame() {
        // Random group size between 2 and 4 players
        int requiredPlayers = 2 + random.nextInt(3);

        // If enough players are waiting, start a game session
        if (waitingPlayers.size() >= requiredPlayers) {
            List<Integer> gamePlayers = new ArrayList<>();
            for (int i = 0; i < requiredPlayers; i++) {
                gamePlayers.add(waitingPlayers.poll());
            }

            int gameId = nextGameId++;
            System.out.println("[GAME SERVER] Starting Game " + gameId + " with " + requiredPlayers + " players: " + gamePlayers);

            // Run each game session in its own independent thread
            new Thread(new GameSession(gameId, gamePlayers, serverClient)).start();
        }
    }

    public static void main(String[] args) {
        try {
            SequenceGameServer server = new SequenceGameServer();
            server.start();
        } catch (IOException e) {
            System.err.println("[GAME SERVER] Could not start: " + e.getMessage());
        }
    }
}