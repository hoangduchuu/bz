package com.ping.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.adapter.SelectContactAdapter;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectContactActivity extends CoreActivity implements View.OnClickListener {

    private RecyclerView rvListContact;
    private LinearLayoutManager mLinearLayoutManager;
    private SearchView searchView;
    private ImageView btBack;
    private Button btSelect;

    private FirebaseAuth auth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    private String selectedId;
    private User currentUser;
    private ArrayList<User> mContacts;
    private SelectContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);
        selectedId = getIntent().getStringExtra("SELECTED_ID");
        bindViews();
        init();
    }

    private void bindViews() {
        btBack = (ImageView) findViewById(R.id.select_contact_back);
        btBack.setOnClickListener(this);
        btSelect = (Button) findViewById(R.id.select_contact_done);
        btSelect.setOnClickListener(this);
        rvListContact = (RecyclerView) findViewById(R.id.select_contact_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(false);
        searchView = (SearchView) findViewById(R.id.select_contact_search_view);
        CommonMethod.UpdateSearchViewLayout(searchView);
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
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        mFirebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        currentUser = ServiceManager.getInstance().getCurrentUser();
        mContacts = new ArrayList<>();
        ArrayList<User> friendList = currentUser.friendList;
        List<String> selectedIDLst = Arrays.asList(selectedId.split(","));
        for (int i = 0; i<selectedIDLst.size(); i++) {
            selectedIDLst.set(i, selectedIDLst.get(i).trim());
        }

        for (User contact : friendList) {
            if (selectedIDLst.contains(contact.key)) {
                continue;
            }
            if (selectedIDLst.contains(contact.pingID)) {
                continue;
            }
            if (selectedIDLst.contains(contact.email)) {
                continue;
            }
            if (StringUtils.isNotEmpty(contact.phone) && selectedIDLst.contains(contact.phone)) {
                continue;
            }
            mContacts.add(contact);
        }

        // TODO SelectContactAdapter
        adapter = new SelectContactAdapter(this, mContacts, null);
        rvListContact.setAdapter(adapter);
        rvListContact.setLayoutManager(mLinearLayoutManager);
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
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
