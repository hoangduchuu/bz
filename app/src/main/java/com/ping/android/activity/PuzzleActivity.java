package com.ping.android.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
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
    private Boolean puzzledstatus;

    private Bitmap originalBitmap;
    private Bitmap puzzledBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        conversationID = getIntent().getStringExtra("CONVERSATION_ID");
        messageID = getIntent().getStringExtra("MESSAGE_ID");
        imageURL = getIntent().getStringExtra("IMAGE_URL");
        puzzledstatus = getIntent().getBooleanExtra("PUZZLE_STATUS", true);
        bindViews();
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (StringUtils.isNotEmpty(conversationID) && StringUtils.isNotEmpty(messageID)) {
            ServiceManager.getInstance().updateMarkStatus(conversationID, messageID, !btPuzzle.isChecked());
        }
    }

    private void bindViews() {
        btBack = (ImageView) findViewById(R.id.puzzle_back);
        btBack.setOnClickListener(this);
        btPuzzle = (ToggleButton) findViewById(R.id.puzzle_toggle);
        btPuzzle.setOnClickListener(this);
        ivPuzzle = (ImageView) findViewById(R.id.puzzle_image);
    }

    private void init() {
        UiUtils.loadImage(ivPuzzle, imageURL, messageID, puzzledstatus, null);
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
        finish();
    }

    public void puzzleImage() {

        UiUtils.loadImage(ivPuzzle, imageURL, messageID, !btPuzzle.isChecked(), null);

        //ivPuzzle.postInvalidate();
        if (StringUtils.isNotEmpty(conversationID) && StringUtils.isNotEmpty(messageID)) {
            ServiceManager.getInstance().updateMarkStatus(conversationID, messageID, !btPuzzle.isChecked());
        }
    }
}
