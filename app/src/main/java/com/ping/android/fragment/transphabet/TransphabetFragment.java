package com.ping.android.fragment.transphabet;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ping.android.activity.R;
import com.ping.android.activity.TransphabetActivity;
import com.ping.android.dagger.loggedin.transphabet.TransphabetComponent;
import com.ping.android.dagger.loggedin.transphabet.selection.TransphabetSelectionComponent;
import com.ping.android.fragment.BaseFragment;
import com.ping.android.service.ServiceManager;
import com.ping.android.utils.UsersUtils;
import com.ping.android.utils.bus.BusProvider;
import com.ping.android.utils.bus.events.TransphabetEvent;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransphabetFragment extends BaseFragment implements View.OnClickListener {

    public static TransphabetFragment newInstance() {
        return new TransphabetFragment();
    }
    @Inject
    BusProvider busProvider;
    TransphabetSelectionComponent component;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transphabet, container, false);
        view.findViewById(R.id.v_generate_random).setOnClickListener(this);
        view.findViewById(R.id.v_manual_setup).setOnClickListener(this);
        view.findViewById(R.id.iv_back).setOnClickListener(this);
        view.findViewById(R.id.v_selective_languages).setOnClickListener(this);
        view.findViewById(R.id.v_selective_emojis).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.v_generate_random:
                showGenerateRandomDialog();
                break;
            case R.id.v_manual_setup:
                moveToFragment(MappingFragment.newInstance());
                break;
            case R.id.iv_back:
                getActivity().onBackPressed();
                break;
            case R.id.v_selective_languages:
                moveToFragment(SelectiveCategoriesFragment.newInstance(false));
                break;
            case R.id.v_selective_emojis:
                moveToFragment(SelectiveCategoriesFragment.newInstance(true));
                break;
        }
    }

    private void moveToFragment(BaseFragment fragment) {
        ((TransphabetActivity)getActivity()).getNavigator().moveToFragment(fragment);
    }

    private void showGenerateRandomDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("CONFIRM")
                .setMessage("Your Transphabet will be randomize. Go to \"Manual Set-up\" to view details.")
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    UsersUtils.randomizeTransphabet();
                    // TODO: should notify conversation fragment to reload
                    busProvider.post(new TransphabetEvent());
                })
                .setNegativeButton(android.R.string.cancel, null).show();

        ServiceManager.getInstance().updateShowMappingConfirm(true);
    }

    public TransphabetSelectionComponent getComponent() {
        if (component == null) {
            component = getComponent(TransphabetComponent.class).provideTransphabetSelectionComponent();
        }
        return component;
    }
}
