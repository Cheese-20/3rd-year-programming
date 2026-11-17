package nmu.wrpv.pubsub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkPubSubClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private int clientId = -1;
    private volatile boolean isRunning = false;

    // Thread-safe map storing topic listener callbacks
    private final ConcurrentHashMap<String, List<MessageListener>> topicListeners = new ConcurrentHashMap<>();

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Read initial ID packet synchronously during handshake
        String initialLine = in.readLine();
        if (initialLine != null && initialLine.startsWith("ID ")) {
            this.clientId = Integer.parseInt(initialLine.substring(3).trim());
        }

        isRunning = true;
        // Start background worker thread for non-blocking message reception
        Thread listenerThread = new Thread(this::listenLoop);
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public int getClientId() {
        return clientId;
    }

    public void subscribe(String topic, MessageListener listener) {
        topicListeners.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(listener);
        if (out != null) {
            out.println("SUB " + topic);
        }
    }

    public void unsubscribe(String topic) {
        topicListeners.remove(topic);
        if (out != null) {
            out.println("UNSUB " + topic);
        }
    }

    public void publish(String topic, String message) {
        if (out != null) {
            out.println("PUB " + topic + " " + message);
        }
    }

    public void disconnect() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }

    private void listenLoop() {
        try {
            String line;
            while (isRunning && (line = in.readLine()) != null) {
                // Protocol: MSG <topic> <publisherId> <messagePayload>
                if (line.startsWith("MSG ")) {
                    String[] parts = line.split(" ", 4);
                    if (parts.length >= 4) {
                        String topic = parts[1];
                        int publisherId = Integer.parseInt(parts[2]);
                        String message = parts[3];

                        List<MessageListener> listeners = topicListeners.get(topic);
                        if (listeners != null) {
                            for (MessageListener listener : listeners) {
                                listener.onMessageReceived(topic, publisherId, message);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("[CLIENT] Network connection lost.");
            }
        } finally {
            disconnect();
        }
    }
}