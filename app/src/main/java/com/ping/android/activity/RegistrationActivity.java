package com.ping.android.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.ultility.Consts;
import com.ping.android.utils.ActivityLifecycle;
import com.ping.android.utils.UiUtils;
import com.quickblox.users.model.QBUser;

public class RegistrationActivity extends CoreActivity implements View.OnClickListener {

    private EditText txtFirstName, txtLastName, txtPingId, txtEmail, txtPassword, txtRetypePassword;
    private TextView tvAgreeTermOfService;
    private CheckBox termCheckBox;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        bindViews();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.registration_back:
                exitRegistration();
                break;
            case R.id.registration_next:
                register();
                break;
            case R.id.tv_register_agree_term_of_service:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_TERMS_OF_SERVICE));
                startActivity(browserIntent);
                break;

        }
    }

    private void bindViews() {
        txtFirstName = (EditText) findViewById(R.id.registration_first_name);
        txtLastName = (EditText) findViewById(R.id.registration_last_name);
        txtPingId = (EditText) findViewById(R.id.registration_ping_id);
        txtEmail = (EditText) findViewById(R.id.registration_email);
        txtPassword = (EditText) findViewById(R.id.registration_password);
        txtRetypePassword = (EditText) findViewById(R.id.registration_retype_password);
        tvAgreeTermOfService = (TextView) findViewById(R.id.tv_register_agree_term_of_service);
        tvAgreeTermOfService.setOnClickListener(this);
        termCheckBox = (CheckBox) findViewById(R.id.registration_terms);
        findViewById(R.id.registration_next).setOnClickListener(this);
        findViewById(R.id.registration_back).setOnClickListener(this);

        UiUtils.setUpHideSoftKeyboard(this, findViewById(R.id.viewRoot));
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
    }

    private void register() {
        String firstName = txtFirstName.getText().toString().trim();
        String lastName = txtLastName.getText().toString().trim();
        final String pingId = txtPingId.getText().toString().trim();
        final String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();
        String retypePassword = txtRetypePassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_name), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!CommonMethod.isValidName(firstName)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_invalid_first_name), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!CommonMethod.isValidName(lastName)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_invalid_last_name), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(pingId)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_ping_id), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!CommonMethod.isValidPingId(pingId)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_invalid_ping_id), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_email), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!CommonMethod.isValidEmail(email)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_invalid_email), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!CommonMethod.isValidPassword(password)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_invalid_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!CommonMethod.isMatchPassword(password, retypePassword)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_mismatch_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!termCheckBox.isChecked()) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_accept_term), Toast.LENGTH_SHORT).show();
            return;
        }

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        showLoading();
        checkDuplicatePingID();
    }

    private void checkDuplicatePingID() {
        final String pingId = txtPingId.getText().toString().trim().toLowerCase();
        mDatabase.child("users").orderByChild("ping_id").equalTo(pingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    checkDuplicateFail(getString(R.string.msg_duplicate_ping_id));
                } else {
                    checkDuplicateEmail();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkDuplicateEmail() {
        final String email = txtEmail.getText().toString().trim().toLowerCase();
        mDatabase.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    checkDuplicateFail(getString(R.string.msg_duplicate_email));
                } else {
                    createAccount();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideLoading();
            }
        });
    }

    private void createAccount() {
        final String firstName = txtFirstName.getText().toString().trim();
        final String lastName = txtLastName.getText().toString().trim();
        final String pingId = txtPingId.getText().toString().trim().toLowerCase();
        final String email = txtEmail.getText().toString().trim().toLowerCase();
        final String password = txtPassword.getText().toString().trim();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            hideLoading();
                            Toast.makeText(RegistrationActivity.this, getString(R.string.msg_create_account_failed),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            createUserProfile(auth.getCurrentUser(), firstName, lastName, pingId, email, password);
                            //sendVerificationEmail(firebaseUser);
                        }
                    }
                });
    }

    private void checkDuplicateFail(String errorMsg) {
        hideLoading();
        Toast.makeText(RegistrationActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
    }

    private void sendVerificationEmail(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegistrationActivity.this, "Please verify register email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void loginAfterRegist() {
        final String firstName = txtFirstName.getText().toString().trim();
        final String lastName = txtLastName.getText().toString().trim();
        final String pingId = txtPingId.getText().toString().trim();
        final String email = txtEmail.getText().toString().trim();
        final String password = txtPassword.getText().toString().trim();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            hideLoading();
                            Toast.makeText(RegistrationActivity.this, getString(R.string.msg_auth_failed), Toast.LENGTH_LONG).show();
                        } else {
                            createUserProfile(auth.getCurrentUser(), firstName, lastName, pingId, email, password);
                        }
                    }
                });
    }

    private void createUserProfile(final FirebaseUser firebaseUser, String firstName, String lastName,
                                   String pingId, String email, String password) {

        firstName = CommonMethod.capitalFirstLetter(firstName);
        lastName = CommonMethod.capitalFirstLetter(lastName);
        User user = new User(firstName, lastName, pingId, email, CommonMethod.encryptPassword(password));
        mDatabase.child("ping_id_lookup").child(pingId).setValue(firebaseUser.getUid()).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                hideLoading();
                Toast.makeText(RegistrationActivity.this, getString(R.string.msg_create_account_failed),
                        Toast.LENGTH_SHORT).show();
                firebaseUser.delete();
            } else {
                mDatabase.child("users").child(firebaseUser.getUid()).setValue(user.toMap()).addOnCompleteListener(RegistrationActivity.this, task1 -> {
                    if (!task1.isSuccessful()) {
                        hideLoading();
                        Toast.makeText(RegistrationActivity.this, getString(R.string.msg_create_account_failed),
                                Toast.LENGTH_SHORT).show();
                        firebaseUser.delete();
                    } else {
                        UserManager.getInstance().initialize((error, data) -> {
                            if (error == null) {
                                hideLoading();
                                ServiceManager.getInstance().updateLoginStatus(true);

                                QBUser qbUser = (QBUser) data[0];
                                startCallService(qbUser);

                                startActivity(new Intent(RegistrationActivity.this, PhoneActivity.class));
                                finish();
                            }
                        });
                    }
                });
            }
        });

    }

    private void startCallService(QBUser qbUser) {
        Intent tempIntent = new Intent(ActivityLifecycle.getForegroundActivity(), CallService.class);
        PendingIntent pendingIntent = ActivityLifecycle.getForegroundActivity().createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(ActivityLifecycle.getForegroundActivity(), qbUser, pendingIntent);
    }

    private void exitRegistration() {
        finish();
    }
}