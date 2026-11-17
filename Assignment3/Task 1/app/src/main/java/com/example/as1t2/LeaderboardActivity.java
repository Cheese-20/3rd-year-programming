package com.example.as1t2;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        LinearLayout container = findViewById(R.id.leaderboardContainer);
        SharedPreferences prefs = getSharedPreferences("Leaderboard", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        List<PlayerWin> winsList = new ArrayList<>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                winsList.add(new PlayerWin(entry.getKey(), (Integer) entry.getValue()));
            }
        }

        // Sort by wins descending
        Collections.sort(winsList, new Comparator<PlayerWin>() {
            @Override
            public int compare(PlayerWin o1, PlayerWin o2) {
                return Integer.compare(o2.wins, o1.wins);
            }
        });

        // Show top 10
        int count = 0;
        for (PlayerWin pw : winsList) {
            if (count >= 10) break;
            TextView tv = new TextView(this);
            tv.setText((count + 1) + ". " + pw.name + ": " + pw.wins + " wins");
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            tv.setPadding(0, 8, 0, 8);
            tv.setGravity(Gravity.CENTER);
            container.addView(tv);
            count++;
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private static class PlayerWin {
        String name;
        int wins;
        PlayerWin(String name, int wins) {
            this.name = name;
            this.wins = wins;
        }
    }
}