package com.ping.android.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ping.android.activity.R;

import org.json.JSONException;
import org.json.JSONObject;

import static org.jivesoftware.smack.roster.packet.RosterPacket.ItemType.from;

/**
 * Created by Tung Tran on 12/2/2017.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            JSONObject notification = new JSONObject( intent.getStringExtra("notification"));
            postNotification(notification, context);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException ex){
            ex.printStackTrace();
        }
    }

    private void postNotification(JSONObject notification, Context context) throws JSONException {

        String title = notification.getString("title");
        String body = notification.getString("body");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(title).
                setContentText(body).
                setSmallIcon(R.mipmap.ic_launcher).
                setAutoCancel(true);
        ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notificationBuilder.build());

    }
}
