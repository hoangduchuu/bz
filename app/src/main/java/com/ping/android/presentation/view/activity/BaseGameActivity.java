package com.ping.android.presentation.view.activity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.ping.android.activity.R;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.GamePresenter;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Constant;

public abstract class BaseGameActivity extends CoreActivity {
    protected ImageView imageView;
    protected String conversationID, messageID;
    protected String imageURL;
    protected Conversation conversation;
    protected User sender;
    protected Handler handler = new Handler(Looper.getMainLooper());

    protected abstract String gameTitle();

    protected void initResources(Intent intent) {
        conversationID = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        messageID = getIntent().getStringExtra("MESSAGE_ID");
        imageURL = getIntent().getStringExtra("IMAGE_URL");
        conversation = getIntent().getParcelableExtra("CONVERSATION");
        sender = getIntent().getParcelableExtra("SENDER");
    }

    protected void onGamePassed() {
        //send game status to sender
        sendNotification(true);
        ServiceManager.getInstance().updateMessageStatus(conversationID, messageID, Constant.MESSAGE_STATUS_GAME_PASS);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    Intent intent = new Intent(this, PuzzleActivity.class);
                    intent.putExtra(ChatActivity.CONVERSATION_ID, conversationID);
                    intent.putExtra("MESSAGE_ID", messageID);
                    intent.putExtra("IMAGE_URL", imageURL);
                    intent.putExtra("PUZZLE_STATUS", false);
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(this, imageView, messageID);
                    startActivity(intent, options.toBundle());
                    //finishAfterTransition();
                    handler.postDelayed(() -> finish(), 2000);
                })
                .setMessage("Congratulations! You won.")
                .setTitle(gameTitle());
        alertDialogBuilder.create().show();
    }

    protected void onGameFailed() {
        //send game status to sender
        sendNotification(false);
        ServiceManager.getInstance().updateMessageStatus(conversationID, messageID, Constant.MESSAGE_STATUS_GAME_FAIL);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(gameTitle())
                .setMessage(R.string.memory_game_game_over)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    finish();
                }).create();
        alertDialog.show();
    }

    protected void quitGame() {
        sendNotification(false);
        ServiceManager.getInstance().updateMessageStatus(conversationID, messageID, Constant.MESSAGE_STATUS_GAME_FAIL);
        finish();
    }

    protected void sendNotification(boolean isPass) {
        if (getPresenter() instanceof GamePresenter) {
            ((GamePresenter) getPresenter()).sendGameStatus(conversation, isPass);
        }
    }
}
