package com.ping.android.presentation.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.ping.android.R;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.SelectContactPresenter;
import com.ping.android.presentation.view.adapter.SelectContactAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class SelectContactActivity extends CoreActivity implements View.OnClickListener, SelectContactPresenter.View {
    public static final String SELECTED_USERS_KEY = "SELECTED_USERS";

    private RecyclerView rvListContact;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText searchView;
    private ImageView btBack;
    private Button btSelect;

    private String selectedId;
    private ArrayList<User> mContacts;
    private SelectContactAdapter adapter;
    private ArrayList<User> selectedUsers;

    @Inject
    SelectContactPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);
        AndroidInjection.inject(this);
        selectedId = getIntent().getStringExtra("SELECTED_ID");
        selectedUsers = getIntent().getParcelableArrayListExtra(SELECTED_USERS_KEY);
        if (selectedUsers == null) {
            selectedUsers = new ArrayList<>();
        }
        bindViews();
        init();
        presenter.create();
    }

    @Override
    public SelectContactPresenter getPresenter() {
        return presenter;
    }

    private void bindViews() {
        btBack = findViewById(R.id.select_contact_back);
        btBack.setOnClickListener(this);
        btSelect = findViewById(R.id.select_contact_done);
        btSelect.setOnClickListener(this);
        rvListContact = findViewById(R.id.select_contact_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(false);
        searchView = findViewById(R.id.select_contact_search_view);
        //CommonMethod.UpdateSearchViewLayout(searchView);
    }

    private void init() {
        mContacts = new ArrayList<>();
        // TODO SelectContactAdapter
        adapter = new SelectContactAdapter(mContacts, (contact, isSelected) -> {
            if (isSelected) {
                selectedUsers.add(contact);
            } else {
                for (User user : selectedUsers) {
                    if (user.key.equals(contact.key)) {
                        selectedUsers.remove(user);
                        break;
                    }
                }
            }
        });
        adapter.setSelectPingIDs(getSelectedPingId());
        rvListContact.setAdapter(adapter);
        rvListContact.setLayoutManager(mLinearLayoutManager);
        registerEvent(
                RxTextView.textChanges(searchView)
                        .subscribe(charSequence -> adapter.filter(charSequence.toString()))
        );
    }

    private List<String> getSelectedPingId() {
        if (selectedUsers != null) {
            List<String> selectedPingId = new ArrayList<>();
            for (User user : selectedUsers) {
                selectedPingId.add(user.pingID);
            }
            return selectedPingId;
        } else {
            return Arrays.asList(selectedId.split(","));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.select_contact_back:
                exitSelectContact();
                break;
            case R.id.select_contact_done:
                chooseContact();
                break;
        }
    }

    private void exitSelectContact() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    private void chooseContact() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("SELECT_CONTACT_PING_IDS", adapter.getSelectPingIDs());
        returnIntent.putExtra("SELECT_CONTACT_USER_IDS", adapter.getSelectUserIDs());
        returnIntent.putParcelableArrayListExtra(SELECTED_USERS_KEY, selectedUsers);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void addFriend(User data) {
        adapter.addContact(data);
    }
}
