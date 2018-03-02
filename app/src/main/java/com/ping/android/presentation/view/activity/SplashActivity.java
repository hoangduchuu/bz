package com.ping.android.presentation.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.ping.android.activity.BeforeLoginActivity;
import com.ping.android.activity.BuildConfig;
import com.ping.android.activity.CoreActivity;
import com.ping.android.activity.MainActivity;
import com.ping.android.activity.R;
import com.ping.android.dagger.loggedout.splash.SplashComponent;
import com.ping.android.dagger.loggedout.splash.SplashModule;
import com.ping.android.managers.UserManager;
import com.ping.android.presentation.presenters.SplashPresenter;

import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends CoreActivity implements SplashPresenter.View {
    @Inject
    public SplashPresenter presenter;
    private SplashComponent component;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        conversationId = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        //app running and logged-in, notification touched
        if(UserManager.getInstance().getUser() != null && StringUtils.isNotEmpty(conversationId)){
            Intent intent2 = new Intent(SplashActivity.this, MainActivity.class);
            intent2.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
            startActivity(intent2);
        }

        setContentView(R.layout.activity_loading);

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);

        //app not running or not logged-in, notification touched or user start app
        if(UserManager.getInstance().getUser() == null) {
            initialize();
        }
        //app running and logged in, user start app
        else if (TextUtils.isEmpty(conversationId)){
            UserManager.getInstance().startCallService();
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initialize() {
        presenter.initializeUser();
    }

    @Override
    public void navigateToMainScreen() {
        Intent intent;
        intent = new Intent(SplashActivity.this, MainActivity.class);
        if (!TextUtils.isEmpty(conversationId)) {
            intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void navigateToLoginScreen() {
        Intent intent;
        intent = new Intent(SplashActivity.this, BeforeLoginActivity.class);
        if (!TextUtils.isEmpty(conversationId)) {
            intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public SplashPresenter getPresenter() {
        return presenter;
    }

    public SplashComponent getComponent() {
        if (component == null) {
            component = getLoggedOutComponent()
                    .provideSplashComponent(new SplashModule(this));
        }
        return component;
    }
}
