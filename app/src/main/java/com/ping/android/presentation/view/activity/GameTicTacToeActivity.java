package com.ping.android.presentation.view.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.dagger.loggedin.game.GameComponent;
import com.ping.android.dagger.loggedin.game.GameModule;
import com.ping.android.model.TicTacToeGame;
import com.ping.android.presentation.presenters.GamePresenter;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GameTicTacToeActivity extends BaseGameActivity implements View.OnClickListener, GamePresenter.View {
    private final TicTacToeGame game = new TicTacToeGame();
    private List<ImageButton> tiles;
    private CountDownTimer gameCountDown;
    private android.os.Vibrator vibrator;
    private TextView tvTimer;

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
        setContentView(R.layout.activity_game_tic_tac_toe);
        initResources(getIntent());
        showStartDialog();
    }

    @Override
    public GamePresenter getPresenter() {
        return presenter;
    }

    private void showStartDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.tic_tac_toe_title)
                .setMessage(R.string.tic_tac_toe_description)
                .setPositiveButton("Start", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    startGame();
                }).create();
        alertDialog.show();
    }

    private void startGame() {
        gameCountDown.start();
    }

    @Override
    protected String gameTitle() {
        return getString(R.string.tic_tac_toe_title);
    }

    @Override
    protected void initResources(Intent intent) {
        super.initResources(intent);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;
        LinearLayout linearLayout = findViewById(R.id.game_container);
        int dimension = widthPixels;
        if (widthPixels > heightPixels) {
            dimension = heightPixels;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dimension, dimension);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        linearLayout.setLayoutParams(params);
        imageView = findViewById(R.id.game_layout_image);
        game.setCurrentPlayer(TicTacToeGame.PLAYER_ONE);
        game.setOnGameOverListener((state, winningIndices) -> {
            if (state == TicTacToeGame.ONE_WINS) {
                onGamePassed();
            } else {
                onGameFailed();
            }
        });
        tiles = new ArrayList<>();
        tiles.add((ImageButton) findViewById(R.id.b00));
        tiles.add((ImageButton) findViewById(R.id.b01));
        tiles.add((ImageButton) findViewById(R.id.b02));
        tiles.add((ImageButton) findViewById(R.id.b03));
        tiles.add((ImageButton) findViewById(R.id.b04));
        tiles.add((ImageButton) findViewById(R.id.b05));
        tiles.add((ImageButton) findViewById(R.id.b06));
        tiles.add((ImageButton) findViewById(R.id.b07));
        tiles.add((ImageButton) findViewById(R.id.b08));
        for (ImageButton button : tiles) {
            button.setOnClickListener(this);
        }

        findViewById(R.id.btn_exit).setOnClickListener(this);
        UiUtils.loadImage(imageView, imageURL, messageID, false, (error, data) -> {
            if (error == null) {
                Bitmap originalBitmap = (Bitmap) data[0];
                imageView.setImageBitmap(originalBitmap);
            }
        });
        tvTimer = findViewById(R.id.game_timer);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        int gameTimeLimit = 20000;
        gameCountDown = new CountDownTimer(gameTimeLimit, 1000) {

            public void onTick(long millisUntilFinished) {
                int remainTime = (int) millisUntilFinished / 1000;
                handler.post(() -> {
                    if (remainTime == 5) {
                        tvTimer.setTextColor(ContextCompat.getColor(GameTicTacToeActivity.this, R.color.red));
                        startVibrate();
                    }
                    tvTimer.setText("" + remainTime);
                });
            }

            public void onFinish() {
                tvTimer.post(() -> tvTimer.setText("" + 0));
                if (!game.isOver()) {
                    onGameFailed();
                }
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

    @Override
    protected void onGamePassed() {
        super.onGamePassed();
        gameCountDown.cancel();
        imageView.setVisibility(View.VISIBLE);
        findViewById(R.id.game_container).setVisibility(View.GONE);
    }

    @Override
    protected void onGameFailed() {
        super.onGameFailed();
        gameCountDown.cancel();
    }

    /**
     * Simulate a CPU move. This can sometimes take awhile, and we also want it to happen after a
     * delay. To do this without blocking the UI thread, RxJava is my go-to tool for threading.
     * Used in tandem with RxLifecycle, we can do this in a non-leaky and responsive way.
     */
    private void simulateCpuMove() {
        registerEvent(game.getCpuMove()
                .subscribeOn(Schedulers.computation())
                .doOnSubscribe(disposable -> {
                })
                .delay(1, TimeUnit.SECONDS)                 // Make it look like the computer is "thinking"
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    // TODO enable buttons click
                    int nextCpuMove = game.getNextCpuMove();
                    // Set image for specific tile
                    setTileView(nextCpuMove, TicTacToeGame.PLAYER_TWO);
                    handleMove(game.getNextCpuMove());
                }));
    }

    private void handleMove(int position) {
        game.makeMove(position);
        char nextPlayer = game.currentPlayer();
        if (!game.isOver()) {
            if (nextPlayer == TicTacToeGame.PLAYER_TWO) {
                simulateCpuMove();
            }
        }
    }

    private void setTileView(int move, char player) {
        ImageButton button = tiles.get(move);
        if (player == TicTacToeGame.PLAYER_ONE) {
            button.setImageResource(R.drawable.cross);
        } else {
            button.setImageResource(R.drawable.nought);
        }
    }

    private void onTileClick(int index) {
        if (index < 0 || !game.isTileAvailable(index)) return;
        setTileView(index, TicTacToeGame.PLAYER_ONE);
        handleMove(index);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_exit) {
            quitGame();
            return;
        }
        // If current player is not opponentUser, return
        if (game.currentPlayer() == TicTacToeGame.PLAYER_TWO) return;
        onTileClick(tiles.indexOf(view));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameCountDown.cancel();
        stopVibrate();
    }
}
