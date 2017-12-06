package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;

import org.apache.commons.lang3.StringUtils;

public class PhoneActivity extends CoreActivity implements View.OnClickListener {

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private CountryCodePicker countryCodePicker;
    private EditText etPhone;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    private void bindViews() {
        findViewById(R.id.phone_register).setOnClickListener(this);
        etPhone = (EditText) findViewById(R.id.phone_number);
        countryCodePicker = (CountryCodePicker) findViewById(R.id.ccp);
    }

    private void init() {
        currentUser = UserManager.getInstance().getUser();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        setDefaultCountryCode();
    }

    private void setDefaultCountryCode() {
        TelephonyManager tm = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso().toUpperCase();
        if (StringUtils.isNoneBlank(countryCode)) {
            countryCodePicker.setDefaultCountryUsingNameCode(countryCode);
            countryCodePicker.resetToDefaultCountry();
        }
    }

    private void registerPhone() {
        String phone = etPhone.getText().toString().trim();
        if (StringUtils.isEmpty(phone)) {
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
        final String phone = countryCodePicker.getSelectedCountryCode() + "-" + etPhone.getText().toString().trim();
        mDatabase.child("users").orderByChild("email").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_duplicate_phone), Toast.LENGTH_SHORT).show();
                } else {
                    currentUser.phone = phone;
                    ServiceManager.getInstance().updatePhone(phone);
                    startActivity(new Intent(PhoneActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
