package com.ping.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.App;
import com.ping.android.CoreApp;
import com.ping.android.domain.usecase.GetCurrentUserUseCase;
import com.ping.android.domain.usecase.InitializeUserUseCase;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.activity.R;

import javax.inject.Inject;

import static com.ping.android.service.NotificationHelper.REPLY_ACTION;

/**
 * Created by bzzz on 2/28/18.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private static String KEY_NOTIFICATION_ID = "key_notification_id";
    private static String KEY_MESSAGE_ID = "key_message_id";

    @Inject
    InitializeUserUseCase initializeUserUseCase;

    public static Intent getReplyMessageIntent(Context context, int notificationId, String messageId) {
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.setAction(REPLY_ACTION);
        intent.putExtra(KEY_NOTIFICATION_ID, notificationId);
        intent.putExtra(KEY_MESSAGE_ID, messageId);
        return intent;
    }

    public NotificationBroadcastReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!REPLY_ACTION.equals(intent.getAction())) {
            return;
        }
        CharSequence message = NotificationHelper.getReplyMessage(intent);
        String messageId = intent.getStringExtra(KEY_MESSAGE_ID);
        if (UserManager.getInstance().getUser() == null){

            ((App) context.getApplicationContext()).getComponent().inject(this);
            initializeUserUseCase.execute(new DefaultObserver<Boolean>() {
                @Override
                public void onNext(Boolean aBoolean) {
                    // do whatever you want with the message. Send to the server or add to the db.
                    // for this tutorial, we'll just show it in a toast;
                    NotificationHelper.getInstance().sendMessage(message.toString(), messageId);

                    // update notification
                    int notifyId = intent.getIntExtra(KEY_NOTIFICATION_ID, 1);
                    updateNotification(context, notifyId);
                }

            }, null);
        }else{
            NotificationHelper.getInstance().sendMessage(message.toString(), messageId);

            // update notification
            int notifyId = intent.getIntExtra(KEY_NOTIFICATION_ID, 1);
            updateNotification(context, notifyId);
        }

    }

    private void updateNotification(Context context, int notifyId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(context.getString(R.string.notif_content_sent));

        notificationManager.notify(notifyId, builder.build());
    }
}
