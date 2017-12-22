package com.ping.android.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.ping.android.service.ServiceManager;
import com.ping.android.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

public class PuzzleActivity extends CoreActivity implements View.OnClickListener {
    private ImageView btBack;
    private ToggleButton btPuzzle;
    private ImageView ivPuzzle;

    private String conversationID, messageID;
    private String imageURL;
    private String localImage;
    private Boolean puzzledstatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        conversationID = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        messageID = getIntent().getStringExtra("MESSAGE_ID");
        imageURL = getIntent().getStringExtra("IMAGE_URL");
        localImage = getIntent().getStringExtra("LOCAL_IMAGE");
        puzzledstatus = getIntent().getBooleanExtra("PUZZLE_STATUS", true);
        supportPostponeEnterTransition();
        bindViews();
        displayImage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (StringUtils.isNotEmpty(conversationID) && StringUtils.isNotEmpty(messageID)) {
            ServiceManager.getInstance().updateMarkStatus(conversationID, messageID, !btPuzzle.isChecked());
        }
    }

    private void bindViews() {
        btBack = findViewById(R.id.puzzle_back);
        btBack.setOnClickListener(this);
        btPuzzle = findViewById(R.id.puzzle_toggle);
        btPuzzle.setOnClickListener(this);
        ivPuzzle = findViewById(R.id.puzzle_image);
        ivPuzzle.setTransitionName(messageID);
    }

    private void displayImage() {
        if (TextUtils.isEmpty(localImage)) {
            UiUtils.loadImage(ivPuzzle, imageURL, messageID, puzzledstatus, (error, data) -> {
                if (error == null) {
                    Bitmap bitmap = (Bitmap) data[0];
                    ivPuzzle.setImageBitmap(bitmap);
                    btPuzzle.setChecked(!puzzledstatus);
                    ivPuzzle.requestLayout();
                }
                supportStartPostponedEnterTransition();
            });
        } else {
            UiUtils.loadImageFromFile(ivPuzzle, localImage, messageID, puzzledstatus, (error, data) -> {
                if (error == null) {
                    Bitmap bitmap = (Bitmap) data[0];
                    ivPuzzle.setImageBitmap(bitmap);
                    btPuzzle.setChecked(!puzzledstatus);
                    ivPuzzle.requestLayout();
                }
                supportStartPostponedEnterTransition();
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.puzzle_back:
                exit();
                break;
            case R.id.puzzle_toggle:
                puzzleImage();
                break;
        }
    }

    public void exit() {
        supportFinishAfterTransition();
    }

    public void puzzleImage() {
        puzzledstatus = !btPuzzle.isChecked();
        displayImage();

        if (StringUtils.isNotEmpty(conversationID) && StringUtils.isNotEmpty(messageID)) {
            ServiceManager.getInstance().updateMarkStatus(conversationID, messageID, !btPuzzle.isChecked());
        }
    }
}
