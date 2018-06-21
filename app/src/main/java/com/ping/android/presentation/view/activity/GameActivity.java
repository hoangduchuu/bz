package com.ping.android.presentation.view.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.dagger.loggedin.game.GameComponent;
import com.ping.android.dagger.loggedin.game.GameModule;
import com.ping.android.presentation.presenters.GamePresenter;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.UiUtils;
import com.ping.android.utils.configs.Constant;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;

public class GameActivity extends BaseGameActivity implements View.OnClickListener, GamePresenter.View {
    private Button btnExit;
    private ImageView puzzleSelect;
    private LinearLayout puzzleView;
    private ImageView puzzleItem1, puzzleItem2, puzzleItem3, puzzleItem4, puzzleItem5, puzzleItem6,
            puzzleItem7, puzzleItem8, puzzleItem0;
    private TextView tvTimer;

    private int puzzleFirst;

    private Bitmap originalBitmap;
    private ArrayList<Bitmap> chunkedImages;
    private ArrayList<ImageView> puzzleItems;
    private ArrayList<Integer> displayOrders;

    private CountDownTimer gameCountDown;
    private Handler handler = new Handler();
    private Vibrator vibrator;

    @Inject
    GamePresenter presenter;
    GameComponent component;

    public GameComponent getComponent() {
        if (component == null) {
            component = getLoggedInComponent().provideGameComponent(new GameModule(this));
        }
        return component;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        setContentView(R.layout.activity_game);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        initResources(getIntent());
        bindViews();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_exit:
                quitGame();
                break;
            case R.id.puzzle_item_0:
                choosePuzzle(view, 0);
                break;
            case R.id.puzzle_item_1:
                choosePuzzle(view, 1);
                break;
            case R.id.puzzle_item_2:
                choosePuzzle(view, 2);
                break;
            case R.id.puzzle_item_3:
                choosePuzzle(view, 3);
                break;
            case R.id.puzzle_item_4:
                choosePuzzle(view, 4);
                break;
            case R.id.puzzle_item_5:
                choosePuzzle(view, 5);
                break;
            case R.id.puzzle_item_6:
                choosePuzzle(view, 6);
                break;
            case R.id.puzzle_item_7:
                choosePuzzle(view, 7);
                break;
            case R.id.puzzle_item_8:
                choosePuzzle(view, 8);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopVibrate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (status == GAME_STARTED) {
            updateGameStatus(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameCountDown.cancel();
        originalBitmap = null;
        for (Bitmap bitmap : chunkedImages) {
            bitmap.recycle();
        }
        chunkedImages.clear();
        chunkedImages = null;
    }

    @Override
    public GamePresenter getPresenter() {
        return presenter;
    }

    private void bindViews() {
        tvTimer = findViewById(R.id.puzzle_timer);
        imageView = findViewById(R.id.game_layout_image);
        imageView.setVisibility(View.GONE);
        puzzleView = findViewById(R.id.game_layout_puzzle);
        btnExit = findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(this);
        puzzleItems = new ArrayList<>();
        puzzleItem0 = findViewById(R.id.puzzle_item_0);
        puzzleItem0.setOnClickListener(this);
        puzzleItem1 = findViewById(R.id.puzzle_item_1);
        puzzleItem1.setOnClickListener(this);
        puzzleItem2 = findViewById(R.id.puzzle_item_2);
        puzzleItem2.setOnClickListener(this);
        puzzleItem3 = findViewById(R.id.puzzle_item_3);
        puzzleItem3.setOnClickListener(this);
        puzzleItem4 = findViewById(R.id.puzzle_item_4);
        puzzleItem4.setOnClickListener(this);
        puzzleItem5 = findViewById(R.id.puzzle_item_5);
        puzzleItem5.setOnClickListener(this);
        puzzleItem6 = findViewById(R.id.puzzle_item_6);
        puzzleItem6.setOnClickListener(this);
        puzzleItem7 = findViewById(R.id.puzzle_item_7);
        puzzleItem7.setOnClickListener(this);
        puzzleItem8 = findViewById(R.id.puzzle_item_8);
        puzzleItem8.setOnClickListener(this);
        puzzleItems.add(puzzleItem0);
        puzzleItems.add(puzzleItem1);
        puzzleItems.add(puzzleItem2);
        puzzleItems.add(puzzleItem3);
        puzzleItems.add(puzzleItem4);
        puzzleItems.add(puzzleItem5);
        puzzleItems.add(puzzleItem6);
        puzzleItems.add(puzzleItem7);
        puzzleItems.add(puzzleItem8);

        showLoading();
        UiUtils.loadImage(imageView, imageURL, messageID, false, (error, data) -> {
            if (error == null) {
                originalBitmap = (Bitmap) data[0];
                displayPuzzle();
            }
            hideLoading();
        });
    }

    @Override
    protected void startGame() {
        super.startGame();
        puzzleView.setVisibility(View.VISIBLE);
        gameCountDown.start();
    }

    private void displayPuzzle() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Start", (dialogInterface, i) -> {
                    startGame();
                }).setMessage("You have 30 seconds to complete this game.")
                .setTitle("PUZZLE GAME");
        alertDialogBuilder.create().show();

        int w = originalBitmap.getWidth();
        int h = originalBitmap.getHeight();
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        if (1.0 * w / h > 1.0 * width / height) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
                    , (int) 1.0 * h * width / w);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            puzzleView.setLayoutParams(layoutParams);
        } else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) 1.0 * w * height / h
                    , RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            puzzleView.setLayoutParams(layoutParams);
        }

        puzzleFirst = -1;
        displayOrders = new ArrayList<>();
        for (int i = 0; i < puzzleItems.size(); i++) {
            displayOrders.add(i);
        }
        Collections.shuffle(displayOrders);
        chunkedImages = CommonMethod.splitImage(originalBitmap, 3);
        for (int i = 0; i < puzzleItems.size(); i++) {
            puzzleItems.get(i).setImageBitmap(chunkedImages.get(displayOrders.get(i)));
        }

        gameCountDown = new CountDownTimer(Constant.GAME_LIMIT_TIME, 1000) {

            public void onTick(long millisUntilFinished) {
                int remainTime = (int) millisUntilFinished / 1000;
                handler.post(() -> {
                    if (remainTime == 5) {
                        tvTimer.setTextColor(ContextCompat.getColor(GameActivity.this, R.color.red));
                        startVibrate();
                    }
                    tvTimer.setText("" + remainTime);
                });
            }

            public void onFinish() {
                tvTimer.post(() -> tvTimer.setText("" + 0));
                updateGameStatus(true);
            }
        };
    }

    private void startVibrate() {
        // Vibrate for 500 milliseconds
        vibrator.vibrate(5000);
    }

    private void stopVibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
        }
    }

    private void choosePuzzle(View view, int index) {
        if (puzzleFirst == -1) {
            puzzleFirst = index;
            puzzleSelect = (ImageView) view;
            puzzleSelect.setBackgroundResource(R.drawable.highlight);
        } else if (puzzleFirst == index) {
            puzzleFirst = -1;
            puzzleSelect.setBackgroundDrawable(null);
            puzzleSelect = null;
        } else {
            int tmp = displayOrders.get(puzzleFirst);
            displayOrders.set(puzzleFirst, displayOrders.get(index));
            displayOrders.set(index, tmp);
            puzzleItems.get(puzzleFirst).setImageBitmap(chunkedImages.get(displayOrders.get(puzzleFirst)));
            puzzleItems.get(index).setImageBitmap(chunkedImages.get(displayOrders.get(index)));
            puzzleFirst = -1;
            puzzleSelect.setBackgroundDrawable(null);
            puzzleSelect = null;
        }
        updateGameStatus(false);
    }

    private boolean checkGameStatus() {
        boolean win = true;
        for (int i = 0; i < displayOrders.size(); i++) {
            if (!displayOrders.get(i).equals(i)) {
                win = false;
            }
        }
        return win;
    }

    private void updateGameStatus(boolean isTimeOut) {
        boolean gameWin = checkGameStatus();
        gameCountDown.cancel();
        stopVibrate();
        if (gameWin) {
            puzzleView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(originalBitmap);
            onGamePassed();
        } else if (isTimeOut) {
            onGameFailed();
        }
    }

    @Override
    protected String gameTitle() {
        return getString(R.string.puzzle_game_title);
    }

}
