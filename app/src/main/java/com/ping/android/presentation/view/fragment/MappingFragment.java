package com.ping.android.presentation.view.fragment;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.dagger.loggedin.transphabet.TransphabetComponent;
import com.ping.android.dagger.loggedin.transphabet.manualmapping.ManualMappingComponent;
import com.ping.android.dagger.loggedin.transphabet.manualmapping.ManualMappingModule;
import com.ping.android.model.Mapping;
import com.ping.android.presentation.presenters.ManualMappingPresenter;

import java.util.List;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class MappingFragment extends BaseFragment implements View.OnClickListener, ManualMappingPresenter.View {
    private List<Mapping> mMappings;

    //Views UI
    private ImageView btBack;
    private Button btReset;

    private View rootView;

    @Inject
    ManualMappingPresenter presenter;
    ManualMappingComponent component;

    public static MappingFragment newInstance() {
        return new MappingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_mapping, container, false);
        bindViews(rootView);
        presenter.create();
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

    private void onClickMapping(String id) {
        if (!id.contains("mapping_")) {
            return;
        }
        char mapKey = id.charAt(id.length() - 1);
        presenter.handleMappingItemClick(mapKey - 'a');
    }

    private void changeMapping(Mapping mapping, String value) {
        presenter.changeMapping(mapping.mapKey, value);
    }

    private void resetMapping() {
        presenter.resetMapping();
    }

    private void bindViews(View view) {
        btBack = view.findViewById(R.id.mapping_back);
        btBack.setOnClickListener(this);
        btReset = view.findViewById(R.id.mapping_reset);
        btReset.setOnClickListener(this);
    }

    @Override
    public void updateMapping(List<Mapping> mappings) {
        for (Mapping mapping : mappings) {
            int resId = getResources().getIdentifier("mapping_" +
                    mapping.mapKey.toLowerCase(), "id", getContext().getPackageName());
            RelativeLayout mappingItem = rootView.findViewById(resId);
            mappingItem.setOnClickListener(this);

            TextView keyTV = mappingItem.findViewById(R.id.mapping_item_key);
            keyTV.setText(mapping.mapKey.toLowerCase());
            TextView valueTV = mappingItem.findViewById(R.id.mapping_item_value);
            if (TextUtils.isEmpty(mapping.mapValue)) {
                keyTV.setBackgroundResource(R.drawable.mapping_main_bg);
                valueTV.setVisibility(View.GONE);
            } else {
                keyTV.setBackgroundResource(R.drawable.mapping_main_bg_active);
                valueTV.setVisibility(View.VISIBLE);
                valueTV.setText(mapping.mapValue);
            }
        }
    }

    @Override
    public void editMappingItem(Mapping mapping) {
        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.dialog_input_mapping, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

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
                if (TextUtils.isEmpty(oldValue) || TextUtils.isEmpty(newValue)) {
                    oldValue = newValue;
                } else if (!TextUtils.equals(oldValue, newValue)) {
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

    public ManualMappingComponent getComponent() {
        if (component == null) {
            component = getComponent(TransphabetComponent.class)
                    .provideManualMappingComponent(new ManualMappingModule(this));
        }
        return component;
    }
}
