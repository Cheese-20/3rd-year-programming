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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GameEventListener{

    private ImageView dice_img1;
    private ImageView dice_img2;
    private SequenceDiceGame game;
    private ArrayList<String> pNames;
    private ArrayList<String> pColors;
    private GridLayout boardGrid;
    private GridLayout playerGrid;
    private Button[][] btnCells = new Button[6][6];
    private Button btnRoll;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //link Java to XML
        dice_img1 = findViewById(R.id.dice_img1);
        dice_img2 = findViewById(R.id.dice_img2);

        //Set the initial disc images
        dice_img1.setImageResource(R.drawable.dice_1);
        dice_img2.setImageResource(R.drawable.dice_1);

        //Get data values from a different intent
        pNames = getIntent().getStringArrayListExtra("PLAYER_NAMES");
        pColors = getIntent().getStringArrayListExtra("PLAYER_COLORS");
        
        if(pNames == null || pNames.isEmpty()) {
            Toast.makeText(this, "Error: No players found",
                    Toast.LENGTH_SHORT).show();
            pNames = new ArrayList<>();
            pNames.add("Player 1");
            pNames.add("Player 2");
        }
        
        if (pColors == null) {
            pColors = new ArrayList<>();
            pColors.add("RED");
            pColors.add("BLUE");
            pColors.add("GREEN");
            pColors.add("PURPLE");
        }

        //Creating the game-grid
        boardGrid = findViewById(R.id.gridBoard);
        playerGrid = findViewById(R.id.gridPlayer);
        btnRoll = findViewById(R.id.btnRollDice);

        try{
         game = new SequenceDiceGame();
         game.addGameEventListener(this);

         ArrayList<Player> players = new ArrayList<>();
         for(int i = 0; i < pNames.size(); i++){
             Token token = Token.valueOf(pColors.get(i));
             players.add(new Player(pNames.get(i), token));
         }

         game.startGame(players);
         setupBoard();
         setupPlayerIndicators();
        } catch (Exception e){
            Toast.makeText(this, "Failed to start the game",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnRoll.setOnClickListener(v -> {
            if(game != null) {
                game.rollDice();
            }
        });
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
                    if (game != null) {
                        game.handleTurn(finalRow, finalCol);
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
                    case RED:
                        button.setBackgroundResource(R.drawable.token_red);
                        break;
                    case BLUE:
                        button.setBackgroundResource(R.drawable.token_blue);
                        break;
                    case GREEN:
                        button.setBackgroundResource(R.drawable.token_green);
                        break;
                    case PURPLE:
                        button.setBackgroundResource(R.drawable.token_purple);
                        break;
                    default:
                        break;
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
            Toast.makeText(this, "Game started! " + currentPlayer.getName() + "'s turn", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onPlayerTurn(Player player) {
        runOnUiThread(() -> {
            setupPlayerIndicators();
            btnRoll.setEnabled(true);
            dice_img1.setImageResource(R.drawable.dice_1);
            dice_img2.setImageResource(R.drawable.dice_1);
            ((TextView) findViewById(R.id.txtTotal)).setText("Total: ?");
            Toast.makeText(this, player.getName() + "'s turn. Roll the dice!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDiceRolled(int die1, int die2, int total) {
        runOnUiThread(() -> {
            btnRoll.setEnabled(false);
            dice_img1.setImageResource(getDiceResource(die1));
            dice_img2.setImageResource(getDiceResource(die2));
            ((TextView) findViewById(R.id.txtTotal)).setText("Total: " + total);
            Toast.makeText(this, "Rolled a " + total + ". Select a cell.", Toast.LENGTH_SHORT).show();
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

    public void onGameOver(Player winner) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Game Over! " + winner.getName() + " wins!", Toast.LENGTH_LONG).show();
            btnRoll.setEnabled(false);
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    btnCells[r][c].setEnabled(false);
                }
            }

            SharedPreferences prefs = getSharedPreferences("Leaderboard", MODE_PRIVATE);
            int wins = prefs.getInt(winner.getName(), 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(winner.getName(), wins + 1);
            editor.apply();

            Intent intent = new Intent(MainActivity.this, GameReportActivity.class);
            intent.putExtra("winner", winner.getName());
            intent.putExtra("rounds", game.roundsPlayed);
            intent.putExtra("tokensPlaced", game.tokensPlaced);
            intent.putExtra("tokensRemoved", game.tokensRemoved);

            for (Token token : Token.values()) {
                if (token != Token.NONE) {
                    intent.putExtra("sequence_" + token.name(),
                            game.longestSequences.getOrDefault(token, 0));
                }
            }
            startActivity(intent);
            finish();
        });
    }
}