package com.ping.android.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.firebase.auth.FirebaseAuth;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.CallService;
import com.ping.android.service.QuickBloxRepository;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Consts;
import com.ping.android.utils.ActivityLifecycle;
import com.ping.android.utils.Log;
import com.quickblox.users.model.QBUser;

import org.apache.commons.lang3.StringUtils;

import io.fabric.sdk.android.Fabric;

public class LoadingActivity extends CoreActivity {
    private FirebaseAuth auth;
    
    private boolean isLogin = false;
    private User user = null;
    private UserRepository userRepository;
    private QuickBloxRepository quickBloxRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String conversationId = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        if(UserManager.getInstance().getUser() != null && StringUtils.isNotEmpty(conversationId)){
            Intent intent2 = new Intent(LoadingActivity.this, MainActivity.class);
            intent2.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
            conversationId = "";
            startActivity(intent2);
        }
        Log.d("loading created");

        setContentView(R.layout.activity_loading);

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);

        userRepository = new UserRepository();
        quickBloxRepository = new QuickBloxRepository();
        if(UserManager.getInstance().getUser() == null) {
            initialize(conversationId);
        }

    }

    private void initialize(String conversationId) {
        Callback qbCallback = (error, data) -> {
            if (error == null) {
                QBUser qbUser = (QBUser) data[0];
                isLogin = true;
                startCallService(qbUser);
            }
            start(conversationId);
        };
        UserManager.getInstance().initialize(qbCallback);
    }

    private void startCallService(QBUser qbUser) {
        Intent tempIntent = new Intent(ActivityLifecycle.getForegroundActivity(), CallService.class);
        PendingIntent pendingIntent = ActivityLifecycle.getForegroundActivity().createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(ActivityLifecycle.getForegroundActivity(), qbUser, pendingIntent);
    }

    private void start(String conversationId) {
        new CountDownTimer(1000, 100) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Intent intent;
                if (isLogin) {
                    intent = new Intent(LoadingActivity.this, MainActivity.class);

                } else {
                    intent = new Intent(LoadingActivity.this, BeforeLoginActivity.class);
                }
                intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
                startActivity(intent);
                finish();
            }
        }.start();
    }

}
