package com.ping.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ping.android.service.NotificationHelper;

import static com.ping.android.service.NotificationHelper.REPLY_ACTION;

/**
 * Created by bzzz on 2/28/18.
 */

public class ReplyActivity extends AppCompatActivity {

    private static final String KEY_MESSAGE_ID = "key_message_id";
    private static final String KEY_NOTIFY_ID = "key_notify_id";

    private String mMessageId;
    private int mNotifyId;

    private ImageButton mSendButton;
    private EditText mEditReply;

    public static Intent getReplyMessageIntent(Context context, int notifyId, String messageId) {
        Intent intent = new Intent(context, ReplyActivity.class);
        intent.setAction(REPLY_ACTION);
        intent.putExtra(KEY_MESSAGE_ID, messageId);
        intent.putExtra(KEY_NOTIFY_ID, notifyId);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        Intent intent = getIntent();

        if (REPLY_ACTION.equals(intent.getAction())) {
            mMessageId = intent.getStringExtra(KEY_MESSAGE_ID);
            mNotifyId = intent.getIntExtra(KEY_NOTIFY_ID, 0);
        }

        mEditReply = findViewById(R.id.edit_reply);
        mSendButton = findViewById(R.id.button_send);

        mSendButton.setOnClickListener(view -> sendMessage(mNotifyId, mMessageId));
    }

    private void sendMessage(int notifyId,  String messageId) {
        // update notification
        updateNotification(notifyId);

        String message = mEditReply.getText().toString().trim();
        // handle send message
        NotificationHelper.getInstance().sendMessage(message, messageId);

        finish();
    }

    private void updateNotification(int notifyId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(getString(R.string.notif_content_sent));

        notificationManager.notify(notifyId, builder.build());
    }
}