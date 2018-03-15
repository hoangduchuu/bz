package com.ping.android.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ping.android.form.Mapping;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.utils.KeyboardHelpers;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class MappingActivity extends CoreActivity implements View.OnClickListener {

    private List<Mapping> mMappings;

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
        btBack = findViewById(R.id.mapping_back);
        btBack.setOnClickListener(this);
        btReset = findViewById(R.id.mapping_reset);
        btReset.setOnClickListener(this);
    }

    private void init() {
        currentUser = UserManager.getInstance().getUser();
        mMappings = ServiceManager.getInstance().getListFromMapping(currentUser.mappings);
        renderMapping();
    }

    private void renderMapping() {
        for (Mapping mapping : mMappings) {
            int resId = getResources().getIdentifier("mapping_" + mapping.mapKey.toLowerCase(), "id", getPackageName());
            RelativeLayout mappingItem = findViewById(resId);
            mappingItem.setOnClickListener(this);

            TextView keyTV = mappingItem.findViewById(R.id.mapping_item_key);
            keyTV.setText(mapping.mapKey.toLowerCase());
            TextView valueTV = mappingItem.findViewById(R.id.mapping_item_value);
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
        KeyboardHelpers.hideSoftInputKeyboard(this);
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

        final EditText userInput = promptsView.findViewById(R.id.input_mapping_value);
        userInput.setText(mapping.mapValue);
        userInput.addTextChangedListener(new TextWatcher() {
            String oldValue = "";
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String newValue = editable.toString();
                if (TextUtils.isEmpty(oldValue) || TextUtils.isEmpty(newValue)){
                    oldValue = newValue;
                }else if(!TextUtils.equals(oldValue, newValue)) {
                    userInput.setText(oldValue);
                }
            }
        });

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
