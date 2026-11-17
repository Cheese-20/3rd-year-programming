package com.example.as1t2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.as1t2.model.Cell;
import com.example.as1t2.model.GameEventListener;
import com.example.as1t2.model.Player;
import com.example.as1t2.model.SequenceDiceGame;
import com.example.as1t2.model.Token;

import java.io.IOException;
import java.util.ArrayList;

import nmu.wrpv.pubsub.NetworkPubSubClient;

public class MainActivity extends AppCompatActivity implements GameEventListener {

    private ImageView dice_img1;
    private ImageView dice_img2;
    private SequenceDiceGame game;
    private ArrayList<String> pNames;
    private ArrayList<String> pColors;
    private GridLayout boardGrid;
    private GridLayout playerGrid;
    private Button[][] btnCells = new Button[6][6];
    private Button btnRoll;

    // Networking
    private NetworkPubSubClient netClient;
    private String gameTopic;
    private int myId;
    private int myPlayerIndex = -1;
    private boolean isMyTurn = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        dice_img1 = findViewById(R.id.dice_img1);
        dice_img2 = findViewById(R.id.dice_img2);
        dice_img1.setImageResource(R.drawable.dice_1);
        dice_img2.setImageResource(R.drawable.dice_1);

        boardGrid = findViewById(R.id.gridBoard);
        playerGrid = findViewById(R.id.gridPlayer);
        btnRoll = findViewById(R.id.btnRollDice);

        game = new SequenceDiceGame();
        game.addGameEventListener(this);

        setupNetwork();

        btnRoll.setOnClickListener(v -> {
            if (isMyTurn && !game.gameActive) { // Waiting for assignment
                 Toast.makeText(this, "Waiting for game to start...", Toast.LENGTH_SHORT).show();
            } else if (isMyTurn) {
                // In networked mode,  the server we want to roll
                game.dice.roll();
                netClient.publish(gameTopic, "ROLL:" + game.dice.getDie1() + ":" + game.dice.getDie2() + ":" + game.dice.getTotal());
            }
        });
    }
    private void setupNetwork() {
        netClient = new NetworkPubSubClient();
        new Thread(() -> {
            try {
                netClient.connect("10.0.2.2", 5555); // Use 10.0.2.2 for Android Emulator to host localhost
                myId = netClient.getClientId();
                
                runOnUiThread(() -> Toast.makeText(this, "Connected! ID: " + myId, Toast.LENGTH_SHORT).show());

                netClient.subscribe("lobby", (topic, senderId, msg) -> {
                    if (msg.startsWith("ASSIGN:" + myId + ":")) {
                        handleAssignment(msg);
                    }
                });

                netClient.publish("lobby", "JOIN");
                runOnUiThread(() -> Toast.makeText(this, "Joined Lobby. Waiting for players...", Toast.LENGTH_LONG).show());

            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void handleAssignment(String msg) {
        // ASSIGN:pId:gameId:myIndex:totalPlayers
        String[] parts = msg.split(":");
        int gameId = Integer.parseInt(parts[2]);
        myPlayerIndex = Integer.parseInt(parts[3]);
        int totalPlayers = Integer.parseInt(parts[4]);
        gameTopic = "game_" + gameId;

        netClient.subscribe(gameTopic, (topic, senderId, message) -> {
            handleGameMessage(senderId, message);
        });

        runOnUiThread(() -> {
            ArrayList<Player> players = new ArrayList<>();
            for (int i = 0; i < totalPlayers; i++) {
                Token t = Token.values()[i + 1]; // Avoid NONE
                players.add(new Player("Player " + (i + 1), t));
            }
            game.startGame(players);
            setupBoard();
            setupPlayerIndicators();
            Toast.makeText(this, "Joined Game #" + gameId + " as Player " + (myPlayerIndex + 1), Toast.LENGTH_SHORT).show();
        });
    }

    private void handleGameMessage(int senderId, String message) {
        String[] parts = message.split(":");
        String cmd = parts[0];

        runOnUiThread(() -> {
            switch (cmd) {
                case "START":
                    int firstPlayerId = Integer.parseInt(parts[2]);
                    isMyTurn = (firstPlayerId == myId);
                    updateTurnUI();
                    break;

                case "ROLLED":
                    // ROLLED:senderId:d1:d2:total
                    int rollerId = Integer.parseInt(parts[1]);
                    int d1 = Integer.parseInt(parts[2]);
                    int d2 = Integer.parseInt(parts[3]);
                    int tot = Integer.parseInt(parts[4]);
                    
                    game.dice.setValues(d1, d2);
                    onDiceRolled(d1, d2, tot);
                    break;

                case "MOVE":
                    // MOVE:senderId:row:col
                    int moverId = Integer.parseInt(parts[1]);
                    int r = Integer.parseInt(parts[2]);
                    int c = Integer.parseInt(parts[3]);
                    
                    game.board.getCell(r, c).setToken(game.players.get(game.currentPlayerIndx).getToken());
                    updateCellAppearance(game.board.getCell(r, c), btnCells[r][c]);
                    break;

                case "TURN":
                    int activeId = Integer.parseInt(parts[1]);
                    isMyTurn = (activeId == myId);
                    game.currentPlayerIndx = (game.currentPlayerIndx + 1) % game.players.size();
                    onPlayerTurn(game.getCurrentPlayer());
                    break;

                case "GAMEOVER":
                    int winnerId = Integer.parseInt(parts[1]);
                    onGameOver(game.getCurrentPlayer()); // Simplified: assumes current player won
                    break;
            }
        });
    }

    private void updateTurnUI() {
        btnRoll.setEnabled(isMyTurn);
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                btnCells[r][c].setEnabled(isMyTurn);
            }
        }
    }

    private void setupBoard() {
        boardGrid.removeAllViews();
        Cell[][] grid = game.getBoard().getGrid();

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                Cell cell = grid[row][col];
                Button cellButton = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(row, 1f),
                        GridLayout.spec(col, 1f)
                );
                params.width = 0;
                params.height = 0;
                params.setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
                cellButton.setLayoutParams(params);
                cellButton.setText(String.valueOf(cell.getValue()));
                cellButton.setTextColor(Color.WHITE);
                cellButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                updateCellAppearance(cell, cellButton);

                btnCells[row][col] = cellButton;

                final int finalRow = row;
                final int finalCol = col;
                cellButton.setOnClickListener(v -> {
                    if (isMyTurn && game.gameActive) {
                        // Send move to server
                        netClient.publish(gameTopic, "MOVE:" + finalRow + ":" + finalCol);
                    }
                });
                boardGrid.addView(cellButton);
            }
        }
    }

    private void setupPlayerIndicators() {
        playerGrid.removeAllViews();
        playerGrid.setColumnCount(game.players.size());

        for (int i = 0; i < game.players.size(); i++) {
            Player player = game.players.get(i);
            TextView playerView = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(0),
                    GridLayout.spec(i, 1f)
            );
            params.width = 0;
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            playerView.setLayoutParams(params);
            playerView.setText(player.getName());
            playerView.setGravity(Gravity.CENTER);
            playerView.setPadding(dpToPx(4), dpToPx(8), dpToPx(4), dpToPx(8));
            playerView.setTextColor(Color.WHITE);
            playerView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            playerView.setBackgroundColor(getPlayerColor(player.getToken()));
            playerView.setAlpha(i == game.currentPlayerIndx ? 1.0f : 0.5f);
            playerGrid.addView(playerView);
        }
    }

    private void updateCellAppearance(Cell cell, Button button) {
        if (cell != null && button != null) {
            if (cell.isEmpty()) {
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700));
                button.setText(String.valueOf(cell.getValue()));
                button.setTextColor(Color.WHITE);
            } else {
                button.setText("");
                switch (cell.getToken()) {
                    case RED: button.setBackgroundResource(R.drawable.token_red); break;
                    case BLUE: button.setBackgroundResource(R.drawable.token_blue); break;
                    case GREEN: button.setBackgroundResource(R.drawable.token_green); break;
                    case PURPLE: button.setBackgroundResource(R.drawable.token_purple); break;
                    default: break;
                }
            }
        }
    }

    private int getPlayerColor(Token token) {
        int colorResId = android.R.color.darker_gray;
        if (token != null) {
            switch (token) {
                case RED: colorResId = R.color.token_red; break;
                case BLUE: colorResId = R.color.token_blue; break;
                case GREEN: colorResId = R.color.token_green; break;
                case PURPLE: colorResId = R.color.token_purple; break;
            }
        }
        return ContextCompat.getColor(this, colorResId);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onGameStarted(Player currentPlayer) {
        runOnUiThread(() -> {
            setupPlayerIndicators();
        });
    }

    @Override
    public void onPlayerTurn(Player player) {
        runOnUiThread(() -> {
            setupPlayerIndicators();
            updateTurnUI();
            dice_img1.setImageResource(R.drawable.dice_1);
            dice_img2.setImageResource(R.drawable.dice_1);
            ((TextView) findViewById(R.id.txtTotal)).setText("Total: ?");
        });
    }

    @Override
    public void onDiceRolled(int die1, int die2, int total) {
        runOnUiThread(() -> {
            dice_img1.setImageResource(getDiceResource(die1));
            dice_img2.setImageResource(getDiceResource(die2));
            ((TextView) findViewById(R.id.txtTotal)).setText("Total: " + total);
        });
    }

    private int getDiceResource(int value) {
        switch (value) {
            case 1: return R.drawable.dice_1;
            case 2: return R.drawable.dice_2;
            case 3: return R.drawable.dice_3;
            case 4: return R.drawable.dice_4;
            case 5: return R.drawable.dice_5;
            case 6: return R.drawable.dice_6;
            default: return R.drawable.dice_1;
        }
    }

    @Override
    public void onTokenPlaced(Player player, int row, int col) {
        runOnUiThread(() -> updateCellAppearance(game.getBoard().getCell(row, col), btnCells[row][col]));
    }

    @Override
    public void onTokenRemoved(Player player, int row, int col) {
        runOnUiThread(() -> updateCellAppearance(game.getBoard().getCell(row, col), btnCells[row][col]));
    }

    @Override
    public void onInvalidMove(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onGameOver(Player winner) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Game Over! " + winner.getName() + " wins!", Toast.LENGTH_LONG).show();
            btnRoll.setEnabled(false);
            // ... (Rest of game over logic)
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (netClient != null) {
            if (gameTopic != null) netClient.publish(gameTopic, "LEAVE");
            netClient.disconnect();
        }
    }
}