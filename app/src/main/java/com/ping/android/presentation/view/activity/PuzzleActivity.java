package com.ping.android.presentation.view.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.ping.android.R;
import com.ping.android.dagger.loggedin.game.GameComponent;
import com.ping.android.dagger.loggedin.game.GameModule;
import com.ping.android.presentation.presenters.GamePresenter;
import com.ping.android.service.ServiceManager;
import com.ping.android.utils.UiUtils;

import javax.inject.Inject;

public class PuzzleActivity extends CoreActivity implements View.OnClickListener, GamePresenter.View {
    private ImageView btBack;
    private ToggleButton btPuzzle;
    private ImageView ivPuzzle;

    private String conversationID, messageID;
    private String imageURL;
    private String localImage;
    private Boolean puzzledstatus;

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
        if (!TextUtils.isEmpty(conversationID) && !TextUtils.isEmpty(messageID)) {
            presenter.updateMessageMask(conversationID, messageID, !btPuzzle.isChecked());
        }
    }

    @Override
    public GamePresenter getPresenter() {
        return presenter;
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

        if (!TextUtils.isEmpty(conversationID) && !TextUtils.isEmpty(messageID)) {
            presenter.updateMessageMask(conversationID, messageID, !btPuzzle.isChecked());
            //ServiceManager.getInstance().updateMarkStatus(conversationID, messageID, !btPuzzle.isChecked());
        }
    }
}
