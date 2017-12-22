package com.ping.android.activity;

import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.service.NotificationHelper;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class GameActivity extends CoreActivity implements View.OnClickListener {
    private FirebaseStorage storage;

    private ImageView btBack, imageView, puzzleSelect;
    private LinearLayout puzzleView;
    private ImageView puzzleItem1, puzzleItem2, puzzleItem3, puzzleItem4, puzzleItem5, puzzleItem6,
            puzzleItem7, puzzleItem8, puzzleItem0;
    private TextView tvTimer;

    private String conversationID, messageID;
    private String imageURL;
    private Conversation conversation;
    private User sender;
    private int puzzleFirst;

    private Bitmap originalBitmap;
    private ArrayList<Bitmap> chunkedImages;
    private ArrayList<ImageView> puzzleItems;
    private ArrayList<Integer> displayOrders;

    private CountDownTimer gameCountDown;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        conversationID = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        messageID = getIntent().getStringExtra("MESSAGE_ID");
        imageURL = getIntent().getStringExtra("IMAGE_URL");
        conversation = getIntent().getParcelableExtra("CONVERSATION");
        sender = getIntent().getParcelableExtra("SENDER");
        bindViews();
        init();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.puzzle_back:
                exit();
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
    protected void onDestroy() {
        super.onDestroy();
        gameCountDown.cancel();
        boolean gameWin = checkGameStatus();
        if (gameWin) {
            ServiceManager.getInstance().updateMessageStatus(conversationID, messageID, Constant.MESSAGE_STATUS_GAME_PASS);
        } else {
            ServiceManager.getInstance().updateMessageStatus(conversationID, messageID, Constant.MESSAGE_STATUS_GAME_FAIL);
        }

        //send game status to sender
        NotificationHelper.getInstance().sendGameStatusNotificationToSender(sender, conversation, gameWin);

        originalBitmap = null;
        for (Bitmap bitmap : chunkedImages) {
            bitmap.recycle();
        }
        chunkedImages.clear();
        chunkedImages = null;
    }

    private void bindViews() {
        tvTimer = (TextView) findViewById(R.id.puzzle_timer);
        imageView = (ImageView) findViewById(R.id.game_layout_image);
        imageView.setVisibility(View.GONE);
        puzzleView = (LinearLayout) findViewById(R.id.game_layout_puzzle);
        btBack = (ImageView) findViewById(R.id.puzzle_back);
        btBack.setOnClickListener(this);
        puzzleItems = new ArrayList<>();
        puzzleItem0 = (ImageView) findViewById(R.id.puzzle_item_0);
        puzzleItem0.setOnClickListener(this);
        puzzleItem1 = (ImageView) findViewById(R.id.puzzle_item_1);
        puzzleItem1.setOnClickListener(this);
        puzzleItem2 = (ImageView) findViewById(R.id.puzzle_item_2);
        puzzleItem2.setOnClickListener(this);
        puzzleItem3 = (ImageView) findViewById(R.id.puzzle_item_3);
        puzzleItem3.setOnClickListener(this);
        puzzleItem4 = (ImageView) findViewById(R.id.puzzle_item_4);
        puzzleItem4.setOnClickListener(this);
        puzzleItem5 = (ImageView) findViewById(R.id.puzzle_item_5);
        puzzleItem5.setOnClickListener(this);
        puzzleItem6 = (ImageView) findViewById(R.id.puzzle_item_6);
        puzzleItem6.setOnClickListener(this);
        puzzleItem7 = (ImageView) findViewById(R.id.puzzle_item_7);
        puzzleItem7.setOnClickListener(this);
        puzzleItem8 = (ImageView) findViewById(R.id.puzzle_item_8);
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
    }

    private void init() {
        UiUtils.loadImage(imageView, imageURL, messageID, false, (error, data) -> {
            if (error == null) {
                originalBitmap = (Bitmap) data[0];
                displayPuzzle();
            }
        });
    }

    private void displayPuzzle() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Start", (dialogInterface, i) -> {
                    puzzleView.setVisibility(View.VISIBLE);
                    gameCountDown.start();
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
                handler.post(() -> tvTimer.setText("" + remainTime));
            }

            public void onFinish() {
                updateGameStatus(true);
            }
        };
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
        if (gameWin || isTimeOut) {
            //send game status to sender
            NotificationHelper.getInstance().sendGameStatusNotificationToSender(sender, conversation, gameWin);
        }

        if (gameWin) {
            ServiceManager.getInstance().updateMessageStatus(conversationID, messageID, Constant.MESSAGE_STATUS_GAME_PASS);
            gameCountDown.cancel();
            puzzleView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(originalBitmap);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(tvTimer.getContext());
            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        Intent intent = new Intent(GameActivity.this, PuzzleActivity.class);
                        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationID);
                        intent.putExtra("MESSAGE_ID", messageID);
                        intent.putExtra("IMAGE_URL", imageURL);
                        intent.putExtra("PUZZLE_STATUS", false);
                        ActivityOptions options = ActivityOptions
                                .makeSceneTransitionAnimation(GameActivity.this, imageView, messageID);
                        startActivity(intent, options.toBundle());
                        //finishAfterTransition();
                        handler.postDelayed(() -> finish(), 2000);
                    }).setMessage("Congratulations! You won.")
                    .setTitle("PUZZLE GAME");
            alertDialogBuilder.create().show();
        } else if (isTimeOut) {
            ServiceManager.getInstance().updateMessageStatus(conversationID, messageID, Constant.MESSAGE_STATUS_GAME_FAIL);
            finish();
        }
    }
}
