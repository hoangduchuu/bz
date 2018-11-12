package com.ping.android.presentation.view.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.R;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ChangePasswordPresenter;
import com.ping.android.utils.BzzzLeftDrawableClickHelper;
import com.ping.android.utils.BzzzViewUtils;
import com.ping.android.utils.CommonMethod;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class ChangePasswordActivity extends CoreActivity implements View.OnClickListener, ChangePasswordPresenter.View {
    private final String TAG = "Ping: " + this.getClass().getSimpleName();

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private EditText etPassword, etNewPassword, etConfirmPassword;
    private User currentUser;
    private List<EditText> editTextList;



    @Inject
    ChangePasswordPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        AndroidInjection.inject(this);
        bindViews();
        init();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                exit();
                break;
            case R.id.password_change:
                changePassword();
                break;
        }
    }

    @Override
    public ChangePasswordPresenter getPresenter() {
        return presenter;
    }

    private void bindViews() {
        findViewById(R.id.password_change).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        etPassword = findViewById(R.id.txt_password);
        etNewPassword = findViewById(R.id.txt_new_password);
        etConfirmPassword = findViewById(R.id.txt_confirm_password);


        setUpRemoveUnderLineIfSelectedEditextAndEyeBall();

    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
    }

    private void changePassword() {
        String password = etPassword.getText().toString();
        String encryptedPassword = CommonMethod.encryptPassword(password);
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!currentUser.password.equals(encryptedPassword)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_wrong_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_new_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!CommonMethod.isValidPassword(newPassword)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_invalid_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_confirm_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_mismatch_new_password), Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.email, password);
        auth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            auth.getCurrentUser().updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            hideLoading();
                                            if (task.isSuccessful()) {
                                                String encryptedNewPassword = CommonMethod.encryptPassword(newPassword);
                                                mDatabase.child("users").child(currentUser.key).child("password").setValue(encryptedNewPassword);
                                                currentUser.password = encryptedNewPassword;
                                                Toast.makeText(getApplicationContext(), getString(R.string.msg_update_password_success), Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(getApplicationContext(), getString(R.string.msg_update_password_fail), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            hideLoading();
                            Toast.makeText(getApplicationContext(), getString(R.string.msg_update_password_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> hideLoading());
    }

    @Override
    public void onUserUpdated(User currentUser) {
        this.currentUser = currentUser;
    }



    private void setUpRemoveUnderLineIfSelectedEditextAndEyeBall() {
        editTextList = new ArrayList<>();
        editTextList.add(etPassword);
        editTextList.add(etNewPassword);
        editTextList.add(etConfirmPassword);
        BzzzViewUtils.INSTANCE.removeUnderLineEditTextIfSelected(editTextList);
        BzzzLeftDrawableClickHelper.showHideButton(this,etPassword,false);
        BzzzLeftDrawableClickHelper.showHideButton(this,etNewPassword,false);
        BzzzLeftDrawableClickHelper.showHideButton(this,etConfirmPassword,false);
        BzzzViewUtils.INSTANCE.showEyeBall(this,editTextList, false);



    }


}
