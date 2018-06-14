package com.ping.android.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.ping.android.R;
import com.ping.android.dagger.loggedout.registration.RegistrationComponent;
import com.ping.android.dagger.loggedout.registration.RegistrationModule;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.RegistrationPresenter;
import com.ping.android.presentation.view.custom.KeyboardAwaredView;
import com.ping.android.presentation.view.custom.KeyboardListener;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.configs.Constant;
import com.ping.android.utils.UiUtils;

import javax.inject.Inject;

public class RegistrationActivity extends CoreActivity implements View.OnClickListener, RegistrationPresenter.View {
    private EditText txtFirstName, txtLastName, txtPingId, txtEmail, txtPassword, txtRetypePassword;
    private TextView tvAgreeTermOfService;
    private CheckBox termCheckBox;
    private KeyboardAwaredView container;
    private LinearLayout bottomLayout;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    @Inject
    public RegistrationPresenter presenter;
    private RegistrationComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
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
            case R.id.tv_login:
                navigateToLoginPage();
                break;
            case R.id.tv_forgot_password:
                navigateToForgotPassword();
                break;
        }
    }

    @Override
    public RegistrationPresenter getPresenter() {
        return presenter;
    }

    private void bindViews() {
        container = findViewById(R.id.container);
        bottomLayout = findViewById(R.id.bottom_layout);
        txtFirstName = findViewById(R.id.registration_first_name);
        txtLastName = findViewById(R.id.registration_last_name);
        txtPingId = findViewById(R.id.registration_ping_id);
        txtEmail = findViewById(R.id.registration_email);
        txtPassword = findViewById(R.id.registration_password);
        txtRetypePassword = findViewById(R.id.registration_retype_password);
        tvAgreeTermOfService = findViewById(R.id.tv_register_agree_term_of_service);
        tvAgreeTermOfService.setOnClickListener(this);
        termCheckBox = findViewById(R.id.registration_terms);
        findViewById(R.id.registration_next).setOnClickListener(this);
        findViewById(R.id.registration_back).setOnClickListener(this);
        findViewById(R.id.tv_login).setOnClickListener(this);
        findViewById(R.id.tv_forgot_password).setOnClickListener(this);
        container.setListener(visible -> {
            if (!visible) {
                // Should hide bottom layout
                bottomLayout.setVisibility(View.VISIBLE);
                //scrollAtBottom = false;
            } else {
                bottomLayout.setVisibility(View.GONE);
            }
        });
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
        String _firstName = txtFirstName.getText().toString().trim().replaceAll(" +", " ");
        final String firstName = CommonMethod.capitalFirstLetters(_firstName);
        String _lastName = txtLastName.getText().toString().trim().replaceAll(" +", " ");
        final String lastName = CommonMethod.capitalFirstLetters(_lastName);
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
                        presenter.initializeUser();
                    }
                });
            }
        });

    }

    private void exitRegistration() {
        finish();
    }

    public RegistrationComponent getComponent() {
        if (component == null) {
            component = getLoggedOutComponent().provideRegistrationComponent(new RegistrationModule(this));
        }
        return component;
    }

    @Override
    public void navigateToMainScreen() {
        startCallService(this);
        Intent intent = new Intent(RegistrationActivity.this, PhoneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLoginPage() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}