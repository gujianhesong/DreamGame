package com.game.dream;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.game.dream.system.SaveSystem;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SaveSystem.getInstance().save(MainActivity.this);
                }
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources to prevent memory leaks
        if (gameView != null) {
            gameView.cleanup();
            gameView = null;
        }
    }
}
