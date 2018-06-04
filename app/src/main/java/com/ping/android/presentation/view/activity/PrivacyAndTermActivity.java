package com.ping.android.presentation.view.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.utils.configs.Constant;
import com.ping.android.utils.Log;

public class PrivacyAndTermActivity extends CoreActivity implements View.OnClickListener {

    private TextView tvCopyRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_and_term);

        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.v_data_privacy).setOnClickListener(this);
        findViewById(R.id.v_terms_of_service).setOnClickListener(this);
        tvCopyRight = findViewById(R.id.tv_copyright);

        intiView();
    }

    private void intiView() {
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            tvCopyRight.setText(getString(R.string.copyright, versionName));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.v_data_privacy:
                Intent privacyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_PRIVACY));
                startActivity(privacyIntent);
                break;

            case R.id.v_terms_of_service:
                Intent termsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_TERMS_OF_SERVICE));
                startActivity(termsIntent);
                break;

            case R.id.iv_back:
                exit();
                break;
        }
    }
}
