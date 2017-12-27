package com.ping.android.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.ping.android.App;
import com.ping.android.activity.ChatActivity;
import com.ping.android.activity.LoadingActivity;
import com.ping.android.activity.MainActivity;
import com.ping.android.activity.R;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.utils.ActivityLifecycle;
import com.ping.android.utils.Log;
import com.ping.android.utils.SharedPrefsHelper;
import com.quickblox.chat.QBChatService;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.chat.Chat;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tung Tran on 12/2/2017.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String message = intent.getStringExtra("data");

            String conversationId = intent.getStringExtra("conversationId");
            String notificationType = intent.getStringExtra("notificationType");
            Log.d("new message: " + message + conversationId + notificationType);
            if (TextUtils.equals(notificationType, "incoming_message")
                    || TextUtils.equals(notificationType, "missed_call")) {
                Log.d("incoming message");
                if (!needDisplayNotification(conversationId)) {
                    return;
                }
                postNotification(message, conversationId, context);
            }
            else if (TextUtils.equals(notificationType, "incoming_call")){
                Log.d("incoming call");
                if (ActivityLifecycle.getInstance().isForeground()){
                    Log.d("app in fore ground, no need to do any thing");
                    return;
                }
                Intent intentNew = new Intent(context, LoadingActivity.class);
                intentNew.putExtra("INCOMING_CALL", "incoming_call");
                //intentNew.addFlags(Intent.FLAG_FROM_BACKGROUND);
                intentNew.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.d("going to start activity");
                context.startActivity(intentNew);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }
    }

    private void postNotification(String message, String conversationId, Context context) throws JSONException {
        User currentUser = UserManager.getInstance().getUser();
        boolean soundNotification = currentUser != null? currentUser.settings.notification: true;

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        // Create pending intent, mention the Activity which needs to be
        Intent intent = new Intent(context, LoadingActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.
                setContentText(message).
                setContentIntent(contentIntent).
                setAutoCancel(true);
        if (App.getActiveActivity() != null && !soundNotification){
            notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
        }else{
            notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }else{
            notificationBuilder
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        //do not show double BZZZ, will change if use title for other meaning
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            notificationBuilder
                    .setContentTitle("BZZZ");
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            notificationBuilder.
                    setSmallIcon(R.drawable.ic_notification).
                    setColor(context.getResources().getColor(R.color.colorAccent)).
                    setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        }else{
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        }
        NotificationManager notificationManager = ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel =
                notificationManager.getNotificationChannel("channel0");
            if (channel == null){
                channel = new NotificationChannel("channel0", "channel0", NotificationManager.IMPORTANCE_HIGH);
                channel.enableLights(true);
                channel.setLightColor(Color.GREEN);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
            notificationBuilder.setChannelId("channel0");
        }

        notificationManager.notify(0, notificationBuilder.build());

    }

    private boolean needDisplayNotification(String conversationId){
        Activity activeActivity = App.getActiveActivity();
        //do not display notification if user already logged out
        if (!SharedPrefsHelper.getInstance().get("isLoggedIn", false)){
            Log.d("user not logged-in");
            return false;
        }
        //do not display notification if user is opening same conversation
        if (activeActivity != null && activeActivity instanceof ChatActivity){
            ChatActivity chatActivity = (ChatActivity) activeActivity;

            if (StringUtils.equals(chatActivity.getConversationId(), conversationId)){
                return false;
            }
        }
        return true;
    }
}
