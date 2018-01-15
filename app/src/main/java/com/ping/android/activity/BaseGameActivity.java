package com.ping.android.activity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Constant;

public abstract class BaseGameActivity extends CoreActivity {
    protected ImageView imageView;
    protected String conversationID, messageID;
    protected String imageURL;
    protected Conversation conversation;
    protected User sender;
    protected Handler handler = new Handler(Looper.getMainLooper());

    protected void initResources(Intent intent) {
        conversationID = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        messageID = getIntent().getStringExtra("MESSAGE_ID");
        imageURL = getIntent().getStringExtra("IMAGE_URL");
        conversation = getIntent().getParcelableExtra("CONVERSATION");
        sender = getIntent().getParcelableExtra("SENDER");
    };

    protected void onGamePassed() {
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
                }).setMessage("Congratulations! You won.")
                .setTitle("MEMORY GAME");
        alertDialogBuilder.create().show();
        ServiceManager.getInstance().updateMessageStatus(conversationID, messageID, Constant.MESSAGE_STATUS_GAME_PASS);
    }

    protected void onGameFailed() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.memory_game_title)
                .setMessage(R.string.memory_game_game_over)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    quitGame();
                }).create();
        alertDialog.show();
    }

    protected void quitGame() {
        ServiceManager.getInstance().updateMessageStatus(conversationID, messageID, Constant.MESSAGE_STATUS_GAME_FAIL);
        finish();
    }
}
