package com.game.dream;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private SurfaceHolder holder;
    private volatile boolean running;
    private GameEngine gameEngine;

    private boolean isFirst;

    public GameView(Context context) {
        super(context);
        this.holder = getHolder();
        this.gameEngine = new GameEngine(context);
    }

    @Override
    public void run() {
        isFirst = true;
        while (running) {
            update(isFirst);
            draw();
            controlFPS();

            isFirst = false;
        }
    }

    private void update(boolean isFirst) {
        gameEngine.update(isFirst);
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            if (canvas != null) {
                gameEngine.draw(canvas);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void controlFPS() {
        try {
            Thread.sleep(16);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (gameEngine != null) {
            gameEngine.cleanup();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gameEngine.handleTouch(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gameEngine.setScreenSize(w, h);
    }
}
