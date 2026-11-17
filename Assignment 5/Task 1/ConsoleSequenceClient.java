package nmu.wrpv.pubsub;
import java.io.IOException;
import java.util.Scanner;

public class ConsoleSequenceClient {
    private final NetworkPubSubClient client;
    private int myId;
    private int myPlayerIndex;
    private String gameTopic = null;
    private boolean isMyTurn = false;
    private volatile boolean gameOver = false;

    public ConsoleSequenceClient() {
        this.client = new NetworkPubSubClient();
    }

    public void start() throws IOException {
        client.connect("localhost", 5555);
        this.myId = client.getClientId();
        System.out.println("=== Sequence-Dice Console Client ===");
        System.out.println("Connected to Broker with ID: " + myId);

        // Listen for matchmaking instructions on lobby
        client.subscribe("lobby", (topic, senderId, msg) -> {
            if (msg.startsWith("ASSIGN:" + myId + ":")) {
                String[] parts = msg.split(":");
                int gameId = Integer.parseInt(parts[2]);
                this.myPlayerIndex = Integer.parseInt(parts[3]);
                this.gameTopic = "game_" + gameId;
                System.out.println("\n[MATCH FOUND] Joined Game #" + gameId + " as Player " + (myPlayerIndex + 1));
                joinGameTopic(gameTopic);
            }
        });

        // Request to join a game
        System.out.println("Entering queue... Waiting for opponents (2-4 players)...");
        client.publish("lobby", "JOIN");

        // Input loop for console player
        Scanner scanner = new Scanner(System.in);
        while (!gameOver) {
            if (isMyTurn) {
                System.out.print("\nYour turn! Enter 'roll' to roll dice: ");
                String input = scanner.nextLine().trim();
                if ("roll".equalsIgnoreCase(input)) {
                    // Logic: roll locally and tell server
                    int d1 = (int)(Math.random() * 6) + 1;
                    int d2 = (int)(Math.random() * 6) + 1;
                    int tot = d1 + d2;
                    client.publish(gameTopic, "ROLL:" + d1 + ":" + d2 + ":" + tot);
                    
                    System.out.print("Enter row and col (e.g. '2 3') or 'quit': ");
                    input = scanner.nextLine().trim();
                    if ("quit".equalsIgnoreCase(input)) {
                        if (gameTopic != null) client.publish(gameTopic, "LEAVE");
                        break;
                    }
                    String[] coords = input.split(" ");
                    if (coords.length == 2) {
                        client.publish(gameTopic, "MOVE:" + coords[0] + ":" + coords[1]);
                        isMyTurn = false;
                    }
                } else if ("quit".equalsIgnoreCase(input)) {
                     if (gameTopic != null) client.publish(gameTopic, "LEAVE");
                     break;
                }
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            }
        }
        client.disconnect();
        System.out.println("Game session closed.");
    }

    private void joinGameTopic(String topic) {
        client.subscribe(topic, (t, senderId, msg) -> {
            String[] parts = msg.split(":");
            switch (parts[0]) {
                case "START":
                    System.out.println("[GAME] Game has started!");
                    if (Integer.parseInt(parts[2]) == myId) {
                        isMyTurn = true;
                    }
                    break;
                case "ROLLED":
                    // ROLLED:senderId:d1:d2:total
                    int rollerId = Integer.parseInt(parts[1]);
                    System.out.println("[GAME] Player " + rollerId + " rolled: " + parts[2] + " + " + parts[3] + " = " + parts[4]);
                    break;
                case "TURN":
                    int activeId = Integer.parseInt(parts[1]);
                    isMyTurn = (activeId == myId);
                    if (!isMyTurn) {
                        System.out.println("[GAME] Opponent's turn (Player ID " + activeId + ")...");
                    }
                    break;
                case "MOVE":
                    // MOVE:senderId:row:col
                    int moverId = Integer.parseInt(parts[1]);
                    if (moverId != myId) {
                        System.out.println("[GAME] Player " + moverId + " placed token at [" + parts[2] + ", " + parts[3] + "]");
                    }
                    break;
                case "GAMEOVER":
                    System.out.println("\n[GAME OVER] Winner is Player ID: " + parts[1]);
                    gameOver = true;
                    break;
                case "PLAYER_DISCONNECTED":
                    System.out.println("\n[NOTICE] An opponent disconnected. Ending game.");
                    gameOver = true;
                    break;
            }
        });
    }

    public static void main(String[] args) {
        try {
            new ConsoleSequenceClient().start();
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}