package com.ping.android.presentation.view.fragment;


import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ping.android.R;
import com.ping.android.presentation.presenters.TransphabetPresenter;
import com.ping.android.presentation.view.activity.TransphabetActivity;
import com.ping.android.utils.UsersUtils;
import com.ping.android.utils.bus.BusProvider;
import com.ping.android.utils.bus.events.TransphabetEvent;

import java.util.Map;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransphabetListFragment extends BaseFragment implements View.OnClickListener, TransphabetPresenter.View {

    public static TransphabetListFragment newInstance() {
        return new TransphabetListFragment();
    }
    @Inject
    BusProvider busProvider;
    @Inject
    TransphabetPresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public TransphabetPresenter getPresenter() {
        return presenter;
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
                    Map<String, String> mappings = UsersUtils.randomizeTransphabet();
                    presenter.randomizeTransphabet(mappings);
                    // TODO: should notify conversation fragment to reload conversations with latest transphabet setting
                    busProvider.post(new TransphabetEvent());
                })
                .setNegativeButton(android.R.string.cancel, null).show();

        //ServiceManager.getInstance().updateShowMappingConfirm(true);
    }
}
