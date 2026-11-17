package com.example.as1t2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_selection);

        Spinner spnPlayerCount = findViewById(R.id.spnPlayerCount);
        Button btnStart = findViewById(R.id.btnNameStart);

        String[] playerOptions = {"2", "3", "4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, playerOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPlayerCount.setAdapter(adapter);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String selected = spnPlayerCount.getSelectedItem().toString();
                int pCount = Integer.parseInt(selected);

                Intent intent = new Intent(SelectionActivity.this, PlayerNameActivity.class);
                intent.putExtra("PLAYER_COUNT", pCount);
                startActivity(intent);
            }
        });
    }
}