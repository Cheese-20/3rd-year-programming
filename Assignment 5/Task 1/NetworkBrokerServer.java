package nmu.wrpv.pubsub;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkBrokerServer {
    private static final int PORT = 5555;
    private static final AtomicInteger idCounter = new AtomicInteger(1);

    // Map topic names to sets of connected client handlers
    private static final ConcurrentHashMap<String, Set<ClientHandler>> topicSubscribers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("[SERVER] Starting Pub/Sub Broker Server on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                int clientId = idCounter.getAndIncrement();
                System.out.println("[SERVER] Client connected: Assigned ID " + clientId);

                ClientHandler handler = new ClientHandler(socket, clientId);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Server exception: " + e.getMessage());
        }
    }

    // Handles individual client socket connections
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final int id;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket, int id) {
            this.socket = socket;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public synchronized void sendMessage(String rawMessage) {
            if (out != null) {
                out.println(rawMessage);
            }
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Handshake: Send assigned ID immediately to the client
                out.println("ID " + id);

                String line;
                while ((line = in.readLine()) != null) {
                    processCommand(line);
                }
            } catch (IOException e) {
                System.out.println("[SERVER] Client " + id + " disconnected.");
            } finally {
                cleanup();
            }
        }

        private void processCommand(String line) {
            String[] parts = line.split(" ", 3);
            if (parts.length == 0) return;
            String command = parts[0].toUpperCase();

            switch (command) {
                case "SUB":
                    if (parts.length >= 2) {
                        String topic = parts[1];
                        topicSubscribers.computeIfAbsent(topic, k -> ConcurrentHashMap.newKeySet()).add(this);
                        System.out.println("[SERVER] Client " + id + " subscribed to topic: " + topic);
                    }
                    break;

                case "UNSUB":
                    if (parts.length >= 2) {
                        String topic = parts[1];
                        Set<ClientHandler> subs = topicSubscribers.get(topic);
                        if (subs != null) {
                            subs.remove(this);
                            System.out.println("[SERVER] Client " + id + " unsubscribed from topic: " + topic);
                        }
                    }
                    break;

                case "PUB":
                    if (parts.length >= 3) {
                        String topic = parts[1];
                        String messagePayload = parts[2];
                        System.out.println("[SERVER] Client " + id + " published to [" + topic + "]: " + messagePayload);
                        broadcast(topic, messagePayload);
                    }
                    break;

                default:
                    System.out.println("[SERVER] Unknown command from Client " + id + ": " + line);
                    break;
            }
        }

        private void broadcast(String topic, String messagePayload) {
            Set<ClientHandler> subs = topicSubscribers.get(topic);
            if (subs != null) {
                // Broadcast to all subscribers including the publisher if subscribed
                String broadcastPacket = "MSG " + topic + " " + id + " " + messagePayload;
                subs.forEach(client -> client.sendMessage(broadcastPacket));
            }
        }

        private void cleanup() {
            // Remove client from all subscription sets
            topicSubscribers.values().forEach(subs -> subs.remove(this));
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
        }
    }
}