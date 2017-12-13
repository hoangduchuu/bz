package com.ping.android.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.model.User;

import org.apache.commons.lang3.StringUtils;

public class ForgotPasswordActivity extends CoreActivity implements View.OnClickListener {

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;

    private EditText txtFirstName, txtLastName, txtEmail, txtPhoneLast4n;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        bindViews();
        init();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.forgot_pass_back:
                exit();
                break;
            case R.id.forgot_pass_reset:
                resetPassword();
                break;
        }
    }

    private void bindViews() {
        findViewById(R.id.forgot_pass_back).setOnClickListener(this);
        findViewById(R.id.forgot_pass_reset).setOnClickListener(this);

        txtEmail = (EditText) findViewById(R.id.forgot_pass_email);
        txtFirstName = (EditText) findViewById(R.id.forgot_pass_first_name);
        txtLastName = (EditText) findViewById(R.id.forgot_pass_last_name);
        txtPhoneLast4n = (EditText) findViewById(R.id.forgot_pass_phone_4n);
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
    }

    private void resetPassword() {
        String phoneLast4n = txtPhoneLast4n.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();

        if (StringUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_email), Toast.LENGTH_SHORT).show();
            return;
        }
        if (StringUtils.isEmpty(phoneLast4n)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_phone_4n), Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading();
        mDatabase.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = new User(dataSnapshot.getChildren().iterator().next());
                    checkResetPassword(user);
                } else {
                    hideLoading();
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_wrong_reset_info), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideLoading();
            }
        });
    }

    private void checkResetPassword(User user) {
        boolean resetInfoFlg = true;
        String firstName = txtFirstName.getText().toString().trim();
        String lastName = txtLastName.getText().toString().trim();
        String phoneLast4n = txtPhoneLast4n.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();

        String phoneLast4nCorrect = user.phone.substring(user.phone.length() - 4, user.phone.length());

        if (!user.email.equals(email)) {
            resetInfoFlg = false;
        }
        if (!phoneLast4nCorrect.equals(phoneLast4n)) {
            resetInfoFlg = false;
        }
        if (StringUtils.isEmpty(user.firstName) && !StringUtils.isEmpty(firstName)) {
            resetInfoFlg = false;
        }
        if (!StringUtils.isEmpty(user.firstName) && !user.firstName.equals(firstName)) {
            resetInfoFlg = false;
        }
        if (StringUtils.isEmpty(user.lastName) && !StringUtils.isEmpty(lastName)) {
            resetInfoFlg = false;
        }
        if (!StringUtils.isEmpty(user.lastName) && !user.lastName.equals(lastName)) {
            resetInfoFlg = false;
        }

        if (!resetInfoFlg) {
            hideLoading();
            Toast.makeText(getApplicationContext(), getString(R.string.msg_wrong_reset_info), Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), getString(R.string.msg_reset_info_success), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.msg_reset_info_fail), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> hideLoading());
    }
}
