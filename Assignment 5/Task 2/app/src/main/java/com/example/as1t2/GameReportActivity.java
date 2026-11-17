package com.example.as1t2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_report);

        String winner = getIntent().getStringExtra("winner");
        int rounds = getIntent().getIntExtra("rounds", 0);
        int placed = getIntent().getIntExtra("tokensPlaced", 0);
        int removed = getIntent().getIntExtra("tokensRemoved", 0);

        TextView txtWinner = findViewById(R.id.txtWinner);
        TextView txtStats = findViewById(R.id.txtStats);
        Button btnBack = findViewById(R.id.btnBackMenu);

        txtWinner.setText("Winner: " + winner);

        StringBuilder stats = new StringBuilder();
        stats.append("Total Rounds: ").append(rounds).append("\n");
        stats.append("Tokens Placed: ").append(placed).append("\n");
        stats.append("Tokens Removed: ").append(removed).append("\n\n");
        stats.append("Longest Sequences:\n");

        String[] tokens = {"RED", "BLUE", "GREEN", "PURPLE"};
        for (String token : tokens) {
            int seq = getIntent().getIntExtra("sequence_" + token, 0);
            if (seq > 0) {
                stats.append("- ").append(token).append(": ").append(seq).append("\n");
            }
        }

        txtStats.setText(stats.toString());

        btnBack.setOnClickListener(v -> {
            // Go back to main menu
            finish();
        });
    }
}