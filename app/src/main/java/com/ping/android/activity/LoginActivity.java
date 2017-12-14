package com.ping.android.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.CallService;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Consts;
import com.ping.android.utils.ActivityLifecycle;
import com.quickblox.users.model.QBUser;

public class LoginActivity extends CoreActivity implements View.OnClickListener {

    private final String TAG = "Ping: " + this.getClass().getSimpleName();
    private FirebaseAuth auth;
    private EditText inputName, inputPassword;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);
        bindViews();
        init();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_back:
                onBack();
                break;
            case R.id.login_next:
                onNext();
                break;
            case R.id.login_forget_password:
                onForgetPassword();
                break;
        }
    }

    private void bindViews() {
        findViewById(R.id.login_back).setOnClickListener(this);
        findViewById(R.id.login_next).setOnClickListener(this);
        findViewById(R.id.login_forget_password).setOnClickListener(this);

        inputName = (EditText) findViewById(R.id.login_name);
        inputPassword = (EditText) findViewById(R.id.login_password);
    }

    private void init() {
        userRepository = new UserRepository();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
    }

    private void onBack() {
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

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(getApplicationContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }

        openLoginByPingId(name);
    }

    private void openLoginByPingId(String name) {
        showLoading();
        mDatabase.child("users").orderByChild("ping_id").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = new User(dataSnapshot.getChildren().iterator().next());
                    openLogin(user.email);
                } else {
                    openLoginByEmail(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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

    private void checkEmailVerified() {
        UserManager.getInstance().initialize(new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                hideLoading();
                if (error == null) {
                    ServiceManager.getInstance().updateLoginStatus(true);

                    QBUser qbUser = (QBUser) data[0];
                    startCallService(qbUser);

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
    }

    private void startCallService(QBUser qbUser) {
        Intent tempIntent = new Intent(ActivityLifecycle.getForegroundActivity(), CallService.class);
        PendingIntent pendingIntent = ActivityLifecycle.getForegroundActivity().createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(ActivityLifecycle.getForegroundActivity(), qbUser, pendingIntent);
    }

    private void onForgetPassword() {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
    }

}

