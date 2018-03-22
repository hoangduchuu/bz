package com.ping.android.presentation.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.ping.android.activity.BuildConfig;
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
        presenter.create();
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
            UserManager.getInstance().startCallService(this);
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
    public void startCallService() {
        UserManager.getInstance().startCallService(this);
    }

    @Override
    public void showAppUpdateDialog(String appId, String currentVersion) {
        new AlertDialog.Builder(this)
                .setTitle("Update Available")
                .setMessage(String.format("A new version of Bzzz is available on Google Play. Please update to version %s now.", currentVersion))
                .setCancelable(false)
                .setPositiveButton("Check it out!", (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("market://details?id=%s", appId)));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        intent.setData(Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s", appId)));
                        startActivity(intent);
                    }
                })
                .create()
                .show();
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
