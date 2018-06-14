package com.ping.android.presentation.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.R;
import com.ping.android.dagger.loggedout.login.LoginComponent;
import com.ping.android.dagger.loggedout.login.LoginModule;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.LoginPresenter;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.KeyboardHelpers;
import com.ping.android.utils.Log;

import java.util.ArrayList;

import javax.inject.Inject;

public class LoginActivity extends CoreActivity implements View.OnClickListener, LoginPresenter.View {
    private final String TAG = "Ping: " + this.getClass().getSimpleName();
    private FirebaseAuth auth;
    private EditText inputName, inputPassword;
    private ImageView bzzzAvatar;

    private DatabaseReference mDatabase;
    private int timesRead = 0;
    ValueEventListener eventListener = null;
    @Inject
    public LoginPresenter presenter;
    private LoginComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);
        bindViews();
        init();
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

        bzzzAvatar = findViewById(R.id.imageView);
        inputName = findViewById(R.id.login_name);
        inputPassword = findViewById(R.id.login_password);
    }

    private void init() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
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

        KeyboardHelpers.hideSoftInputKeyboard(this);

        if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }

        openLoginByPingId(name);
    }

    private void openLoginByPingId(String name) {
        timesRead = 0;
        showLoading();
        ArrayList<Query> queries = new ArrayList<>();
        queries.add(mDatabase.child("users").orderByChild("ping_id").equalTo(name));
        queries.add(mDatabase.child("users").orderByChild("email").equalTo(name));
        queries.add(mDatabase.child("users").orderByChild("phone").equalTo(name));
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timesRead++;
                if (dataSnapshot.exists()) {
                    User user = new User(dataSnapshot.getChildren().iterator().next());
                    if(eventListener != null) {
                        for (Query query: queries
                             ) {
                            query.removeEventListener(eventListener);
                        }
                    }
                    openLogin(user.email);
                } else if (timesRead == 3) {
                    if(eventListener != null) {
                        for (Query query: queries
                                ) {
                            query.removeEventListener(eventListener);
                        }
                    }
                    openLoginFail();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                openLoginFail();
            }
        };
        for (Query query: queries
             ) {
            query.addValueEventListener(eventListener);
        }
    }

    private void openLoginByEmail(String name) {
        mDatabase.child("users").orderByChild("email").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = new User(dataSnapshot.getChildren().iterator().next());
                    openLogin(user.email);
                } else {
                    openLoginByPhone(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void openLoginByPhone(String name) {
        mDatabase.child("users").orderByChild("phone").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = new User(dataSnapshot.getChildren().iterator().next());
                    openLogin(user.email);
                } else {
                    openLoginFail();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                openLoginFail();
            }
        });
    }

    private void openLoginFail() {
        hideLoading();
        Toast.makeText(getApplicationContext(), getString(R.string.msg_auth_info_incorrect), Toast.LENGTH_SHORT).show();
    }

    private void openLogin(String email) {
        final String password = inputPassword.getText().toString().trim();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, getString(R.string.msg_auth_failed), Toast.LENGTH_LONG).show();
                            hideLoading();
                        } else {
                            checkEmailVerified();
                        }
                    }
                });
    }

    @Override
    public LoginPresenter getPresenter() {
        return presenter;
    }

    private void checkEmailVerified() {
        presenter.initializeUser();
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
                        auth.sendPasswordResetEmail(email.getText().toString())
                                .addOnSuccessListener(aVoid -> Log.d("Success"))
                                .addOnFailureListener(e -> Log.e(e));
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
        startCallService(this);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public LoginComponent getComponent() {
        if (component == null) {
            component = getLoggedOutComponent().provideLoginComponent(new LoginModule(this));
        }
        return component;
    }
}

