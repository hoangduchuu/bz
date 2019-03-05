package com.ping.android.presentation.view.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.ping.android.R;
import com.ping.android.presentation.presenters.LoginPresenter;
import com.ping.android.utils.BzzzLeftDrawableClickHelper;
import com.ping.android.utils.BzzzViewUtils;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class LoginActivity extends CoreActivity implements View.OnClickListener, LoginPresenter.View {
    private final String TAG = "Ping: " + this.getClass().getSimpleName();
    private EditText inputName, inputPassword;



    @Inject
    public LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        setContentView(R.layout.activity_login);
        bindViews();
    }

    @Override
    protected void onDestroy() {
        findViewById(R.id.tv_register).setOnClickListener(null);
        findViewById(R.id.login_next).setOnClickListener(null);
        findViewById(R.id.tv_forgot_password).setOnClickListener(null);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_next:
                onNext();
                break;
            case R.id.tv_register:
                onRegisterPressed();
                break;
            case R.id.tv_forgot_password:
                onForgetPassword();
                break;
        }
    }

    private void bindViews() {
        findViewById(R.id.tv_register).setOnClickListener(this);
        findViewById(R.id.login_next).setOnClickListener(this);
        findViewById(R.id.tv_forgot_password).setOnClickListener(this);

        inputName = findViewById(R.id.login_name);
        inputPassword = findViewById(R.id.login_password);


        setUpRemoveUnderLineIfSelectedEditext();
        setUpShowEyeBallIfSelectedEditext();
    }



    private void onRegisterPressed() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        Bundle options = ActivityOptionsCompat
                .makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
                //.makeSceneTransitionAnimation(this, bzzzAvatar, "bzzz")
                .toBundle();
        startActivity(intent, options);
        finish();
    }

    private void onNext() {
        final String name = inputName.getText().toString().trim().toLowerCase();
        final String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_username), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_password), Toast.LENGTH_SHORT).show();
            return;
        }

        //KeyboardHelpers.hideSoftInputKeyboard(this);

        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        presenter.login(name, password);
        //openLoginByPingId(name);
    }

    @Override
    public LoginPresenter getPresenter() {
        return presenter;
    }

    private void onForgetPassword() {
        View promptsView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);
        EditText email = promptsView.findViewById(R.id.email);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.forgot_password_dialog_title)
                .setView(promptsView)
                .setPositiveButton("SEND", (dialog12, which) -> {
                    String emailString = email.getText().toString();
                    if (CommonMethod.isValidEmail(emailString)) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                                .addOnSuccessListener(aVoid -> Log.e("Success"))
                                .addOnFailureListener(e -> Log.e("Failed: " + e));
                    } else {
                        showEmailInvalidDialog();
                    }
                })
                .setNegativeButton("CANCEL", (dialog1, which) -> dialog1.dismiss())
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void showEmailInvalidDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("WARNING")
                .setMessage("Verify your email address")
                .setPositiveButton("OK", (dialog1, which) -> dialog1.dismiss())
                .create();
        dialog.show();
    }

    @Override
    public void navigateToMainScreen() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        //finish();
    }

    @Override
    public void showMessageLoginFailed() {
        Toast.makeText(getApplicationContext(), getString(R.string.msg_auth_info_incorrect), Toast.LENGTH_SHORT).show();
    }

    private void setUpRemoveUnderLineIfSelectedEditext() {
        List<EditText> editTextList = new ArrayList<>();
        editTextList.add(inputName);
        editTextList.add(inputPassword);
        BzzzViewUtils.INSTANCE.removeUnderLineEditTextIfSelected(editTextList);


    }

    private void setUpShowEyeBallIfSelectedEditext() {
        List<EditText> editTextList = new ArrayList<>();
        List<TextInputLayout> til = new ArrayList<>();
        editTextList.add(inputPassword);
//        BzzzViewUtils.INSTANCE.showEyeBallWhenTextbox(til,editTextList);
        BzzzLeftDrawableClickHelper.showHideButton(this,inputPassword,true);
        BzzzViewUtils.INSTANCE.showEyeBall(this,editTextList, true);

    }

    /**
     * Detect event when click outside the TextBox to hide the keyboard
     * @param event is event of motion to detect
     * @return just require from class, don't use
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (view instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            Objects.requireNonNull(w).getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < w.getLeft() || x >= w.getRight()
                    || y < w.getTop() || y > w.getBottom())) {
                hideKeyboard();
            }
            return ret;
        }
        return ret;
    }

    @Override
    public void showTimeOutNotification() {
        super.showTimeOutNotification();
    }
}

