package com.ping.android;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;
import com.ping.android.activity.BuildConfig;
import com.ping.android.model.QbConfigs;
import com.ping.android.service.NotificationBroadcastReceiver;
import com.ping.android.utils.configs.CoreConfigUtils;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.ServiceZone;
import com.quickblox.core.SubscribePushStrategy;
import com.quickblox.messages.services.QBPushManager;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;


public class CoreApp extends Application {
    public static final String TAG = CoreApp.class.getSimpleName();
    private static final String QB_CONFIG_DEFAULT_FILE_NAME = "qb_config.json";
    private static CoreApp instance;
    private QbConfigs qbConfigs;

    public static synchronized CoreApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initQbConfigs();
        initCredentials();
        initPushManager();
        EmojiManager.install(new GoogleEmojiProvider());

        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
    }

    private void initQbConfigs() {
        Log.i(TAG, "QB CONFIG FILE NAME: " + getQbConfigFileName());
        qbConfigs = CoreConfigUtils.getCoreConfigsOrNull(getQbConfigFileName());
    }

    public void initCredentials() {
        if (qbConfigs != null) {
            QBSettings.getInstance().init(getApplicationContext(), qbConfigs.getAppId(), qbConfigs.getAuthKey(), qbConfigs.getAuthSecret());
            QBSettings.getInstance().setAccountKey(qbConfigs.getAccountKey());
            QBSettings.getInstance().setEnablePushNotification(true);
            QBSettings.getInstance().setSubscribePushStrategy(SubscribePushStrategy.ALWAYS);

            if (!TextUtils.isEmpty(qbConfigs.getApiDomain()) && !TextUtils.isEmpty(qbConfigs.getChatDomain())) {
                //QBSettings.getInstance().setEndpoints(qbConfigs.getApiDomain(), qbConfigs.getChatDomain(), ServiceZone.DEVELOPMENT);
                //QBSettings.getInstance().setZone(ServiceZone.DEVELOPMENT);
                ServiceZone serviceZone = BuildConfig.DEBUG ? ServiceZone.DEVELOPMENT: ServiceZone.PRODUCTION;
                QBSettings.getInstance().setEndpoints(qbConfigs.getApiDomain(), qbConfigs.getChatDomain(), serviceZone);
                QBSettings.getInstance().setZone(serviceZone);
            }
            QBPushManager.getInstance().addListener(new QBPushManager.QBSubscribeListener() {
                @Override
                public void onSubscriptionCreated() {
                    Log.d(TAG, "onSubscriptionCreated");
                }

                @Override
                public void onSubscriptionError(Exception e, int i) {
                    Log.e(TAG, "onSubscriptionError" + e);
                    if (i >= 0) {
                        Log.e(TAG, "Google play service exception " + i);
                    }
                    Log.e(TAG, "onSubscriptionError " + e.getMessage());
                }

                @Override
                public void onSubscriptionDeleted(boolean b) {
                    Log.e(TAG, "onSubscriptionDeleted " + b);
                }
            });
        }
    }

    private void initPushManager() {
        QBPushManager.getInstance().addListener(new QBPushManager.QBSubscribeListener() {
            @Override
            public void onSubscriptionCreated() {
                //Toaster.shortToast("Subscription Created");
                Log.d(TAG, "SubscriptionCreated");
            }

            @Override
            public void onSubscriptionError(Exception e, int resultCode) {
                Log.d(TAG, "SubscriptionError" + e.getLocalizedMessage());
                if (resultCode >= 0) {
                    String error = GoogleApiAvailability.getInstance().getErrorString(resultCode);
                    Log.d(TAG, "SubscriptionError playServicesAbility: " + error);
                }
                //Toaster.shortToast(e.getLocalizedMessage());
            }

            @Override
            public void onSubscriptionDeleted(boolean b) {

            }
        });

    }

    public QbConfigs getQbConfigs() {
        return qbConfigs;
    }

    protected String getQbConfigFileName() {
        return QB_CONFIG_DEFAULT_FILE_NAME;
    }
}