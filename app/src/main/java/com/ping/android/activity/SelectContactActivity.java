package com.ping.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ping.android.presentation.view.adapter.SelectContactAdapter;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.ultility.CommonMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectContactActivity extends CoreActivity implements View.OnClickListener {
    public static final String SELECTED_USERS_KEY = "SELECTED_USERS";

    private RecyclerView rvListContact;
    private LinearLayoutManager mLinearLayoutManager;
    private SearchView searchView;
    private ImageView btBack;
    private Button btSelect;

    private String selectedId;
    private User currentUser;
    private ArrayList<User> mContacts;
    private SelectContactAdapter adapter;
    private ArrayList<User> selectedUsers;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);
        selectedId = getIntent().getStringExtra("SELECTED_ID");
        selectedUsers = getIntent().getParcelableArrayListExtra(SELECTED_USERS_KEY);
        if (selectedUsers == null) {
            selectedUsers = new ArrayList<>();
        }
        bindViews();
        init();
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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
        searchView.setOnClickListener(v -> searchView.setIconified(false));
    }

    private void init() {
        currentUser = UserManager.getInstance().getUser();
        mContacts = new ArrayList<>(currentUser.friendList);

        // TODO SelectContactAdapter
        adapter = new SelectContactAdapter(this, mContacts, new SelectContactAdapter.ClickListener() {
            @Override
            public void onSelect(User contact, Boolean isSelected) {
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
            }
        });
        adapter.setSelectPingIDs(getSelectedPingId());
        rvListContact.setAdapter(adapter);
        rvListContact.setLayoutManager(mLinearLayoutManager);
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
}
