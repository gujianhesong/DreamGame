package com.game.dream;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.game.dream.system.SaveSystem;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            SaveSystem.getInstance().readLatest(getApplicationContext());
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        Button saveListButton = findViewById(R.id.saveListButton);
        saveListButton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, SaveListActivity.class);
            startActivity(intent);
        });
    }
}
