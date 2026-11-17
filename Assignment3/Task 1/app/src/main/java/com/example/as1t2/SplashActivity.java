package com.example.as1t2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Close and go to main menu after 3 seconds or on tap
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startMainMenu();
            }
        };

        handler.postDelayed(runnable, 3000);

        findViewById(R.id.splashLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                startMainMenu();
            }
        });
    }

    private void startMainMenu() {
        Intent intent = new Intent(SplashActivity.this, MainMenuActivity.class);
        startActivity(intent);
        finish();
    }
}