package com.ping.android.fragment.transphabet;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ping.android.activity.R;
import com.ping.android.adapter.LanguageTransphabetAdapter;
import com.ping.android.fragment.BaseFragment;
import com.ping.android.model.Language;
import com.ping.android.service.ServiceManager;
import com.ping.android.utils.DataProvider;
import com.ping.android.utils.UsersUtils;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectiveLanguagesFragment extends BaseFragment implements View.OnClickListener, LanguageTransphabetAdapter.OnClickListener {

    public static SelectiveLanguagesFragment newInstance() {
        return new SelectiveLanguagesFragment();
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

        RecyclerView recyclerView = view.findViewById(R.id.language_list);
        List<Language> languages = DataProvider.getLanguages();
        LanguageTransphabetAdapter adapter = new LanguageTransphabetAdapter(languages);
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onClick(Language language) {
        if (getContext() == null) return;

        String description = String.format(getString(R.string.language_transphabet_confirmation), language.name);
        new AlertDialog.Builder(getContext())
                .setTitle("Warning")
                .setMessage(description)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> UsersUtils.randomizeTransphabet(language))
                .setNegativeButton(android.R.string.cancel, null).show();
    }
}
