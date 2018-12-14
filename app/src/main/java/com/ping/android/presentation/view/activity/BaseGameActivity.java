package com.ping.android.presentation.view.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.CallSuper;
import androidx.annotation.IntDef;
import android.widget.ImageView;

import com.ping.android.R;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.enums.Color;
import com.ping.android.presentation.presenters.GamePresenter;
import com.ping.android.utils.configs.Constant;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseGameActivity extends CoreActivity {
    private static final int GAME_INIT = 0;
    protected static final int GAME_STARTED = 1;
    private static final int GAME_PASSED = 2;
    private static final int GAME_FAILED = 3;

    protected ImageView imageView;
    protected String conversationID, messageID;
    protected String imageURL;
    protected Message message;
    protected Conversation conversation;
    protected Handler handler = new Handler(Looper.getMainLooper());
    protected Color currentColor = Color.DEFAULT;
    @GameStatus protected int status = GAME_INIT;

    protected abstract String gameTitle();

    protected void initResources(Intent intent) {
        conversationID = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        messageID = getIntent().getStringExtra("MESSAGE_ID");
        imageURL = getIntent().getStringExtra("IMAGE_URL");
        message = getIntent().getParcelableExtra("MESSAGE");
        conversation = getIntent().getParcelableExtra("CONVERSATION");
        if (getIntent().hasExtra(ChatActivity.EXTRA_CONVERSATION_COLOR)) {
            int color = getIntent().getIntExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, 0);
            currentColor = Color.from(color);
        }
    }

    @CallSuper
    protected void startGame() {
        status = GAME_STARTED;
    }

    protected void onGamePassed() {
        status = GAME_PASSED;
        //send game status to sender
        sendNotification(true);

        updateMessageStatus(Constant.MESSAGE_STATUS_GAME_PASS);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(gameTitle())
                .setMessage("Congratulations! You won.")
                .setNegativeButton("OK",(dialog, i)->{
                    List<Message> messages = new ArrayList<>();
                    messages.add(message);
                    GroupImageGalleryActivity.start(this, conversationID, messages, 0, false, null);
                    finishAfterTransition();
                }).create();
        alertDialog.show();

        setDialogColor(alertDialog);


    }

    private void setDialogColor(AlertDialog alertDialog) {
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.white));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.white));
    }

    protected void onGameFailed() {
        status = GAME_FAILED;
        //send game status to sender
        sendNotification(false);
        updateMessageStatus(Constant.MESSAGE_STATUS_GAME_FAIL);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(gameTitle())
                .setMessage(R.string.memory_game_game_over)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    finish();
                }).create();
        alertDialog.show();

        setDialogColor(alertDialog);
    }

    protected void quitGame() {
        status = GAME_FAILED;
        sendNotification(false);
        updateMessageStatus(Constant.MESSAGE_STATUS_GAME_FAIL);
        finish();
    }

    private void sendNotification(boolean isPass) {
        if (getPresenter() instanceof GamePresenter) {
            ((GamePresenter) getPresenter()).sendGameStatus(conversation, isPass);
        }
    }

    private void updateMessageStatus(int status) {
        if (getPresenter() instanceof GamePresenter) {
            ((GamePresenter) getPresenter()).updateMessageStatus(conversationID, messageID, status);
            if (status == Constant.MESSAGE_STATUS_GAME_PASS) {
                message.isMask = false;
                ((GamePresenter) getPresenter()).updateMessageMask(conversationID, messageID, false);
            }
        }
    }

    @IntDef({ GAME_INIT, GAME_STARTED, GAME_PASSED, GAME_FAILED })
    private @interface GameStatus {}
}
