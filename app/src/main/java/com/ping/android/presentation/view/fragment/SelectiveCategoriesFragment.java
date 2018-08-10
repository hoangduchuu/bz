package com.ping.android.presentation.view.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ping.android.R;
import com.ping.android.presentation.presenters.TransphabetPresenter;
import com.ping.android.presentation.view.adapter.TransphabetCategoryAdapter;
import com.ping.android.model.Transphabet;
import com.ping.android.utils.DataProvider;
import com.ping.android.utils.UsersUtils;
import com.ping.android.utils.bus.BusProvider;
import com.ping.android.utils.bus.events.TransphabetEvent;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectiveCategoriesFragment extends BaseFragment implements View.OnClickListener,
        TransphabetCategoryAdapter.OnClickListener, TransphabetPresenter.View {
    public static final String SELECTIVE_CATEGORY_KEY = "SELECTIVE_CATEGORY_KEY";

    private boolean isEmoji = false;

    @Inject
    BusProvider busProvider;
    @Inject
    TransphabetPresenter presenter;

    public static SelectiveCategoriesFragment newInstance(boolean isEmoji) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SELECTIVE_CATEGORY_KEY, isEmoji);
        SelectiveCategoriesFragment fragment = new SelectiveCategoriesFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
        if (getArguments() != null) {
            isEmoji = getArguments().getBoolean(SELECTIVE_CATEGORY_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_selective_languages, container, false);
        setupView(view);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                getActivity().onBackPressed();
        }
    }

    public void setupView(View view) {
        view.findViewById(R.id.iv_back).setOnClickListener(this);
        ((TextView) view.findViewById(R.id.tv_title)).setText(isEmoji ? R.string.setting_selective_emojis : R.string.setting_selective_languages);

        RecyclerView recyclerView = view.findViewById(R.id.language_list);
        List<Transphabet> transphabets = isEmoji ? DataProvider.getEmojis() : DataProvider.getLanguages();
        TransphabetCategoryAdapter adapter = new TransphabetCategoryAdapter(transphabets);
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onClick(Transphabet transphabet) {
        if (getContext() == null) return;

        String description = String.format(getString(R.string.language_transphabet_confirmation), transphabet.name);
        new AlertDialog.Builder(getContext())
                .setTitle("Warning")
                .setMessage(description)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    Map<String, String> mappings;
                    if (isEmoji) {
                        mappings = UsersUtils.randomizeEmojiTransphabet(transphabet);
                    } else {
                        mappings = UsersUtils.randomizeTransphabet(transphabet);
                    }
                    presenter.randomizeTransphabet(mappings);
                    busProvider.post(new TransphabetEvent());
                    getActivity().onBackPressed();
                })
                .setNegativeButton(android.R.string.cancel, null).show();
    }
}
