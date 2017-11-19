package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class BeforeLoginActivity extends CoreActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_before_login);
        bindViews();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loading_sign_in:
                onSignIn();
                break;
            case R.id.loading_sign_up:
                onSignUp();
                break;
        }
    }

    private void bindViews() {
        findViewById(R.id.loading_sign_in).setOnClickListener(this);
        findViewById(R.id.loading_sign_up).setOnClickListener(this);
    }

    private void onSignIn() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void onSignUp() {
        startActivity(new Intent(this, RegistrationActivity.class));
    }
}
