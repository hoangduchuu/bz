package com.ping.android.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ping.android.App;
import com.quickblox.chat.QBChatService;
import com.quickblox.users.model.QBUser;

import javax.inject.Inject;

/**
 * QuickBlox team
 */
public class CallService extends Service {
    private static final String TAG = CallService.class.getSimpleName();

    public static final String ACTION_LOG_IN = "com.ping.android.ACTION_LOG_IN";
    public static final String ACTION_LOG_OUT = "com.ping.android.ACTION_LOG_OUT";

    public static final String EXTRA_QB_ID = "EXTRA_QB_ID";
    public static final String EXTRA_PING_ID = "EXTRA_PING_ID";

    @Inject
    CallServiceHandler handler;

    public static void start(Context context, QBUser qbUser, PendingIntent pendingIntent) {
        Intent intent = new Intent(context, CallService.class);
        Log.d(TAG, "going to start call service");
        intent.putExtra(EXTRA_QB_ID, qbUser.getId());
        intent.putExtra(EXTRA_PING_ID, qbUser.getLogin());
        intent.setAction(ACTION_LOG_IN);
        //intent.putExtra(Consts.EXTRA_PENDING_INTENT, pendingIntent);
        context.startService(intent);
    }

    public static void start(Context context, Integer qbId, String pingId) {
        Intent intent = new Intent(context, CallService.class);
        Log.d(TAG, "going to start call service");
        intent.putExtra(EXTRA_QB_ID, qbId);
        intent.putExtra(EXTRA_PING_ID, pingId);
        intent.setAction(ACTION_LOG_IN);
        //intent.putExtra(Consts.EXTRA_PENDING_INTENT, pendingIntent);
        context.startService(intent);
    }

    public static void logout(Context context) {
        Intent intent = new Intent(context, CallService.class);
        //intent.putExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_LOGOUT);
        intent.setAction(ACTION_LOG_OUT);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((App) getApplication()).getComponent().inject(this);
        handler.create();
        initChatService();

        Log.d(TAG, "Service onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        if (intent != null && intent.getAction() != null) {
            if (ACTION_LOG_IN.equals(intent.getAction())) {
                logUserIn(intent);
            }
            if (ACTION_LOG_OUT.equals(intent.getAction())) {
                logUserOut();
            }
        }
//        parseIntentExtras(intent);
//
//        startSuitableActions();

        return START_STICKY;
    }

    private void logUserOut() {
        handler.logout();
        //stopSelf();
    }

    private void logUserIn(Intent intent) {
        String pingId = intent.getStringExtra(EXTRA_PING_ID);
        int qbId = intent.getIntExtra(EXTRA_QB_ID, -1);
        if (qbId > 0) {
            handler.loginUser(qbId, pingId);
        }
    }

    private void initChatService() {
        QBChatService.setDebugEnabled(true);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy()");
        super.onDestroy();
        handler.destroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service onBind)");
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "Service onTaskRemoved()");
        super.onTaskRemoved(rootIntent);
//        destroyRtcClientAndChat();
    }
}
