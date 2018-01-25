package com.ping.android.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.ping.android.model.TicTacToeGame;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GameTicTacToeActivity extends BaseGameActivity implements View.OnClickListener {
    private final TicTacToeGame game = new TicTacToeGame();
    private List<ImageButton> tiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_tic_tac_toe);
        initResources(getIntent());
        startGame();
    }

    private void startGame() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.tic_tac_toe_title)
                .setMessage(R.string.tic_tac_toe_description)
                .setPositiveButton("Start", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).create();
        alertDialog.show();
    }

    @Override
    protected void initResources(Intent intent) {
        super.initResources(intent);
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
        tiles.add(findViewById(R.id.b00));
        tiles.add(findViewById(R.id.b01));
        tiles.add(findViewById(R.id.b02));
        tiles.add(findViewById(R.id.b03));
        tiles.add(findViewById(R.id.b04));
        tiles.add(findViewById(R.id.b05));
        tiles.add(findViewById(R.id.b06));
        tiles.add(findViewById(R.id.b07));
        tiles.add(findViewById(R.id.b08));
        for (ImageButton button: tiles) {
            button.setOnClickListener(this);
        }

        findViewById(R.id.btn_exit).setOnClickListener(this);
        UiUtils.loadImage(imageView, imageURL, messageID, false, (error, data) -> {
            if (error == null) {
                Bitmap originalBitmap = (Bitmap) data[0];
                imageView.setImageBitmap(originalBitmap);
            }
        });
    }

    @Override
    protected void onGamePassed() {
        super.onGamePassed();
        imageView.setVisibility(View.VISIBLE);
        findViewById(R.id.game_container).setVisibility(View.GONE);
    }

    /**
     * Simulate a CPU move. This can sometimes take awhile, and we also want it to happen after a
     * delay. To do this without blocking the UI thread, RxJava is my go-to tool for threading.
     * Used in tandem with RxLifecycle, we can do this in a non-leaky and responsive way.
     */
    private void simulateCpuMove() {
        game.getCpuMove()
                .subscribeOn(Schedulers.computation())
                .doOnSubscribe(disposable -> {})
                .delay(1, TimeUnit.SECONDS)                 // Make it look like the computer is "thinking"
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    // TODO enable buttons click
                    int nextCpuMove = game.getNextCpuMove();
                    // Set image for specific tile
                    setTileView(nextCpuMove, TicTacToeGame.PLAYER_TWO);
                    handleMove(game.getNextCpuMove());
                });
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
        // If current player is not user, return
        if (game.currentPlayer() == TicTacToeGame.PLAYER_TWO) return;
        onTileClick(tiles.indexOf(view));
    }
}
