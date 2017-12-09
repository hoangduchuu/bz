package com.ping.android.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.form.Mapping;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class MappingActivity extends CoreActivity implements View.OnClickListener {

    private List<Mapping> mMappings;

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    //Views UI
    private ImageView btBack;
    private Button btReset;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapping);
        bindViews();
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveMapping();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveMapping();
    }

    private void bindViews() {
        btBack = (ImageView) findViewById(R.id.mapping_back);
        btBack.setOnClickListener(this);
        btReset = (Button) findViewById(R.id.mapping_reset);
        btReset.setOnClickListener(this);
    }

    private void init() {
        currentUser = UserManager.getInstance().getUser();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        mMappings = ServiceManager.getInstance().getListFromMapping(currentUser.mappings);
        renderMapping();
    }

    private void renderMapping() {
        for (Mapping mapping : mMappings) {
            int resId = getResources().getIdentifier("mapping_" + mapping.mapKey.toLowerCase(), "id", getPackageName());
            RelativeLayout mappingItem = (RelativeLayout) findViewById(resId);
            mappingItem.setOnClickListener(this);

            TextView keyTV = (TextView) mappingItem.findViewById(R.id.mapping_item_key);
            keyTV.setText(mapping.mapKey.toLowerCase());
            TextView valueTV = (TextView) mappingItem.findViewById(R.id.mapping_item_value);
            if (StringUtils.isEmpty(mapping.mapValue)) {
                keyTV.setBackgroundResource(R.drawable.mapping_main_bg);
                valueTV.setVisibility(View.GONE);
            } else {
                keyTV.setBackgroundResource(R.drawable.mapping_main_bg_active);
                valueTV.setVisibility(View.VISIBLE);
                valueTV.setText(mapping.mapValue);
            }
        }
    }

    private void resetMapping() {
        mMappings = ServiceManager.getInstance().getDefaultMappingList();
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        renderMapping();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mapping_back:
                onExitMapping();
                break;
            case R.id.mapping_reset:
                resetMapping();
                break;
            default:
                String id = getResources().getResourceName(view.getId());
                onClickMapping(id);
                break;
        }
    }

    private void onClickMapping(String id) {
        if (!id.contains("mapping_")) {
            return;
        }
        char mapKey = id.charAt(id.length() - 1);
        Mapping mapping = mMappings.get(mapKey - 'a');

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_input_mapping, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.input_mapping_value);
        userInput.setText(mapping.mapValue);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String text = userInput.getText().toString();
                                changeMapping(mapping, text);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        userInput.setSelection(0, mapping.mapValue.length());
        // show it
        alertDialog.show();
    }

    private void changeMapping(Mapping mapping, String value) {
        if (!mapping.mapValue.equals(value)) {
            mapping.mapValue = value;
            renderMapping();
            saveMapping();
        }
    }

    private void saveMapping() {
        currentUser.mappings = ServiceManager.getInstance().getMappingFromList(mMappings);
        ServiceManager.getInstance().updateMapping(currentUser.mappings);
    }

    private void onExitMapping() {
        saveMapping();
        finish();
    }
}
