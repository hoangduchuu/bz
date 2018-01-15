package com.ping.android.activity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class GameMemoryActivity extends BaseGameActivity implements View.OnClickListener {
    private static final int GAME_STEPS = 6;
    private ImageButton imgRed;
    private ImageButton imgGreen;
    private ImageButton imgYellow;
    private ImageButton imgBlue;

    private Map<GamePattern, MediaPlayer> mediaPlayerMap;
    private ArrayList<GamePattern> originalSteps;
    private AtomicInteger step;
    private Timer timer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean playingMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_memory);
        initResources(getIntent());

        showStartGameDialog();
    }

    private void generateGamePatterns() {
        originalSteps = new ArrayList<>();
        for (int i = 0; i < GAME_STEPS - 1; i++) {
            originalSteps.add(GamePattern.random());
        }
    }

    private void playOriginalPatterns() {
        timer = new Timer();
        step = new AtomicInteger(0);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (step.get() < originalSteps.size()) {
                    GamePattern gamePattern = originalSteps.get(step.get());
                    handler.post(() -> playGamePattern(gamePattern));
                    playSound(gamePattern);
                    step.incrementAndGet();
                } else {
                    timer.cancel();
                    step.set(0);
                    playingMode = true;
                }
            }
        };
        timer.scheduleAtFixedRate(task, 500, 500);
    }

    private void playSound(GamePattern pattern) {
        MediaPlayer mp = mediaPlayerMap.get(pattern);
        if (mp != null) {
            mp.seekTo(0);
            mp.start();
        }
    }

    private void playGamePattern(GamePattern pattern) {
        View view = getViewFromPattern(pattern);
        view.setPressed(true);
        handler.postDelayed(() -> view.setPressed(false), 300);
    }

    private boolean checkPattern(GamePattern gamePattern) {
        return originalSteps.get(step.get()) == gamePattern;
    }

    private View getViewFromPattern(GamePattern pattern) {
        View view = null;
        switch (pattern) {
            case RED:
                view = imgRed;
                break;
            case BLUE:
                view = imgBlue;
                break;
            case GREEN:
                view = imgGreen;
                break;
            case YELLOW:
                view = imgYellow;
                break;
        }
        return view;
    }

    private void playGame(GamePattern gamePattern) {
        if (!playingMode || step.get() >= originalSteps.size()) return;
        if (checkPattern(gamePattern)) {
            playGamePattern(gamePattern);
            playSound(gamePattern);
            if (step.incrementAndGet() == originalSteps.size()) {
                onGamePassed();
            }
        } else {
            onGameFailed();
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.red:
                playGame(GamePattern.RED);
                break;
            case R.id.yellow:
                playGame(GamePattern.YELLOW);
                break;
            case R.id.blue:
                playGame(GamePattern.BLUE);
                break;
            case R.id.green:
                playGame(GamePattern.GREEN);
                break;
            case R.id.btn_exit:
                quitGame();
                break;
        }
    }

    @Override
    protected void initResources(Intent intent) {
        super.initResources(intent);
        imgRed = findViewById(R.id.red);
        imgBlue = findViewById(R.id.blue);
        imgYellow = findViewById(R.id.yellow);
        imgGreen = findViewById(R.id.green);
        imageView = findViewById(R.id.image_original);

        imgRed.setOnClickListener(this);
        imgBlue.setOnClickListener(this);
        imgYellow.setOnClickListener(this);
        imgGreen.setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);

        mediaPlayerMap = new HashMap<>();
        mediaPlayerMap.put(GamePattern.RED, MediaPlayer.create(this, R.raw.game_1));
        mediaPlayerMap.put(GamePattern.GREEN, MediaPlayer.create(this, R.raw.game_2));
        mediaPlayerMap.put(GamePattern.YELLOW, MediaPlayer.create(this, R.raw.game_3));
        mediaPlayerMap.put(GamePattern.BLUE, MediaPlayer.create(this, R.raw.game_4));

        generateGamePatterns();

        UiUtils.loadImage(imageView, imageURL, messageID, false, (error, data) -> {
            if (error == null) {
                Bitmap originalBitmap = (Bitmap) data[0];
                imageView.setImageBitmap(originalBitmap);
            }
            hideLoading();
        });
    }

    private void showStartGameDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.memory_game_title)
                .setMessage(R.string.memory_game_start_instruction)
                .setPositiveButton("Start", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    handler.postDelayed(this::playOriginalPatterns, 500);
                }).create();
        alertDialog.show();
    }

    public enum GamePattern {
        RED, YELLOW, BLUE, GREEN;

        public static GamePattern random() {
            return GamePattern.values()[new Random().nextInt(GamePattern.values().length)];
        }
    }
}
