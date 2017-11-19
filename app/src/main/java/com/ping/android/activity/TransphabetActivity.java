package com.ping.android.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.ping.android.service.ServiceManager;
import com.ping.android.utils.UsersUtils;

public class TransphabetActivity extends CoreActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transphabet);

        findViewById(R.id.v_generate_random).setOnClickListener(this);
        findViewById(R.id.v_manual_setup).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.v_generate_random:
                showGenerateRandomDialog();
                break;
            case R.id.v_manual_setup:
                startActivity(new Intent(TransphabetActivity.this, MappingActivity.class));
                break;
            case R.id.iv_back:
                exit();
                break;
        }
    }

    private void showGenerateRandomDialog() {
        new AlertDialog.Builder(TransphabetActivity.this)
                .setTitle("CONFIRM")
                .setMessage("Your Transphabet will be randomize. Go to \"Manual Set-up\" to view details.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        UsersUtils.randomizeTransphabet();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).show();

        ServiceManager.getInstance().updateShowMappingConfirm(true);
    }


}
