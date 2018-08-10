package com.ping.android.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.ping.android.R;
import com.ping.android.presentation.presenters.AddPhonePresenter;
import com.ping.android.utils.CommonMethod;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class PhoneActivity extends CoreActivity implements View.OnClickListener, AddPhonePresenter.View {

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private CountryCodePicker countryCodePicker;
    private EditText etPhone;

    @Inject
    AddPhonePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
        setContentView(R.layout.activity_phone);
        bindViews();
        init();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.phone_register:
                registerPhone();
                break;
        }
    }

    @Override
    public AddPhonePresenter getPresenter() {
        return presenter;
    }

    private void bindViews() {
        findViewById(R.id.phone_register).setOnClickListener(this);
        etPhone = findViewById(R.id.phone_number);
        countryCodePicker = findViewById(R.id.ccp);
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        setDefaultCountryCode();
    }

    private void setDefaultCountryCode() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso().toUpperCase();
        if (!TextUtils.isEmpty(countryCode)) {
            countryCodePicker.setDefaultCountryUsingNameCode(countryCode);
            countryCodePicker.resetToDefaultCountry();
        }
    }

    private void registerPhone() {
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_empty_phone), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!CommonMethod.isValidPhone(phone)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_invalid_phone), Toast.LENGTH_SHORT).show();
            return;
        }
        checkDuplicatePhone();
    }

    private void checkDuplicatePhone() {
        showLoading();
        final String phone = countryCodePicker.getSelectedCountryCode() + "-" + etPhone.getText().toString().trim();
        mDatabase.child("users").orderByChild("email").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hideLoading();
                if (dataSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_duplicate_phone), Toast.LENGTH_SHORT).show();
                } else {
                    presenter.updatePhone(phone);
                    startActivity(new Intent(PhoneActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideLoading();
            }
        });
    }

}
