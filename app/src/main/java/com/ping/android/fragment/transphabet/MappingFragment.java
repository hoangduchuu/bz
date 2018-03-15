package com.ping.android.fragment.transphabet;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.form.Mapping;
import com.ping.android.fragment.BaseFragment;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.utils.KeyboardHelpers;
import com.vanniktech.emoji.EmojiEditText;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MappingFragment extends BaseFragment implements View.OnClickListener {
    private List<Mapping> mMappings;

    //Views UI
    private ImageView btBack;
    private Button btReset;

    private User currentUser;

    private View rootView;

    public static MappingFragment newInstance() {
        return new MappingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_mapping, container, false);
        bindViews(rootView);
        init();
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mapping_back:
                getActivity().onBackPressed();
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

    private void renderMapping() {
        for (Mapping mapping : mMappings) {
            int resId = getResources().getIdentifier("mapping_" + mapping.mapKey.toLowerCase(), "id", getContext().getPackageName());
            RelativeLayout mappingItem = rootView.findViewById(resId);
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

    private void onClickMapping(String id) {
        if (!id.contains("mapping_")) {
            return;
        }
        char mapKey = id.charAt(id.length() - 1);
        Mapping mapping = mMappings.get(mapKey - 'a');

        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.dialog_input_mapping, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EmojiEditText userInput = promptsView.findViewById(R.id.input_mapping_value);
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
        int length = userInput.getText().length();
        userInput.setSelection(0, length);
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

    private void resetMapping() {
        mMappings = ServiceManager.getInstance().getDefaultMappingList();
        KeyboardHelpers.hideSoftInputKeyboard(getActivity());
        renderMapping();
    }

    private void bindViews(View view) {
        btBack = view.findViewById(R.id.mapping_back);
        btBack.setOnClickListener(this);
        btReset = view.findViewById(R.id.mapping_reset);
        btReset.setOnClickListener(this);
    }

    private void init() {
        currentUser = UserManager.getInstance().getUser();
        mMappings = ServiceManager.getInstance().getListFromMapping(currentUser.mappings);
        renderMapping();
    }
}
