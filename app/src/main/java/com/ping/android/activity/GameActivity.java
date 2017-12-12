package com.ping.android.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

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
    private String imageURL, imageLocalName, imageLocalPath, imageLocalFolder;

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
        originalBitmap.recycle();
        originalBitmap = null;
        for (Bitmap bitmap : chunkedImages) {
            bitmap.recycle();
            bitmap = null;
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
        storage = FirebaseStorage.getInstance();
        imageLocalPath = this.getExternalFilesDir(null).getAbsolutePath();
        imageLocalName = CommonMethod.getFileNameFromFirebase(imageURL);
        imageLocalPath = imageLocalPath + File.separator + imageLocalName;
        File imageLocal = new File(imageLocalPath);
        imageLocalName = imageLocal.getName();
        imageLocalFolder = imageLocal.getParent();
        CommonMethod.createFolder(imageLocalFolder);

        if (imageLocal.exists()) {
            originalBitmap = BitmapFactory.decodeFile(imageLocalPath);
            displayPuzzle();
        } else {
            StorageReference imageReference = storage.getReferenceFromUrl(imageURL);
            imageReference.getFile(imageLocal).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    originalBitmap = BitmapFactory.decodeFile(imageLocalPath);
                    displayPuzzle();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

        }
    }

    private void displayPuzzle() {
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
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        tvTimer.setText("" + remainTime);
                    }
                });
            }

            public void onFinish() {
                boolean gameWin = checkGameStatus();
                if (gameWin) {
                    ServiceManager.getInstance().updateMessageStatus(conversationID, messageID, Constant.MESSAGE_STATUS_GAME_PASS);
                } else {
                    ServiceManager.getInstance().updateMessageStatus(conversationID, messageID, Constant.MESSAGE_STATUS_GAME_FAIL);
                }
                finish();
            }
        }.start();
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
        boolean gameWin = checkGameStatus();
        if (gameWin) {
            gameCountDown.cancel();
            puzzleView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(originalBitmap);
        }
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
}
