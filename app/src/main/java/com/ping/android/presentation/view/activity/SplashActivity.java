package com.ping.android.presentation.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.ping.android.R;
import com.ping.android.presentation.presenters.SplashPresenter;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class SplashActivity extends CoreActivity implements SplashPresenter.View {
    @Inject
    public SplashPresenter presenter;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        AndroidInjection.inject(this);
        presenter.create();
        conversationId = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        if (!TextUtils.isEmpty(conversationId)) {
            presenter.handleNewConversation(conversationId);
        } else {
            initialize();
        }
    }

    private void initialize() {
        presenter.initializeUser();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void navigateToMainScreen() {
        Intent intent;
        intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void navigateToLoginScreen() {
        Intent intent;
        intent = new Intent(SplashActivity.this, RegistrationActivity.class);
        if (!TextUtils.isEmpty(conversationId)) {
            intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
        }
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this,
                android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(intent, bundle);
        finish();
    }

    @Override
    public void startCallService() {
        //startCallService(this);
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
    public void navigateToMainScreenWithExtra(String conversationId) {
        Intent intent2 = new Intent(SplashActivity.this, MainActivity.class);
        intent2.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
        startActivity(intent2);
        finish();
    }

    @Override
    public SplashPresenter getPresenter() {
        return presenter;
    }
}
