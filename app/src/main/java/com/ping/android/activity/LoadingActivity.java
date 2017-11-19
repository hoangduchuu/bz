package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;

import io.fabric.sdk.android.Fabric;

public class LoadingActivity extends CoreActivity {
    private FirebaseAuth auth;
    
    private boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(BuildConfig.DEBUG)
                .build();
        Fabric.with(fabric);

        setContentView(R.layout.activity_loading);

        load();
    }

    private void load() {
        auth = FirebaseAuth.getInstance();
        if (auth != null && auth.getCurrentUser() != null) {
            ServiceManager.getInstance().initUserData(new Callback() {
                @Override
                public void complete(Object error, Object... data) {
                    User currentUser = ServiceManager.getInstance().getCurrentUser();
                    if (currentUser.quickBloxID == null || currentUser.quickBloxID == 0) {
                        ServiceManager.getInstance().signUpNewUserQB();
                    } else {
                        ServiceManager.getInstance().loadQBUser();
                    }
                }
            });
            isLogin = true;
            start();
        } else {
            start();
        }
    }

    private void start() {
        new CountDownTimer(1000, 100) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (isLogin) {
                    startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(LoadingActivity.this, BeforeLoginActivity.class));
                    finish();
                }
            }
        }.start();
    }

}
