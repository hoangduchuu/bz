package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.adapter.BlockAdapter;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BlockActivity extends CoreActivity implements View.OnClickListener, BlockAdapter.ClickListener {

    private final String TAG = "Ping: " + this.getClass().getSimpleName();
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private DatabaseReference mBlockDatabase;
    private ChildEventListener observeBlockEvent;
    private FirebaseUser mFirebaseUser;

    private RecyclerView rvListBlock;
    private LinearLayoutManager mLinearLayoutManager;

    private BlockAdapter adapter;
    private User currentUser;
    private ArrayList<User> blockContacts = new ArrayList<>();
    private List<String> blockIds = new ArrayList<>();

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block);
        bindViews();
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.SELECT_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                //ArrayList<String> selectContacts = data.getStringArrayListExtra("SELECT_CONTACT_USER_IDS");
                //blockContact(selectContacts);
                //blockIds.addAll(selectContacts);
                List<User> selectedUsers = data.getParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY);
                blockContact(selectedUsers);
            }
        }
    }

    private void bindViews() {
        rvListBlock = (RecyclerView) findViewById(R.id.block_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);

        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.block_save).setOnClickListener(this);
        findViewById(R.id.block_add).setOnClickListener(this);
    }

    private void init() {
        userRepository = new UserRepository();
        auth = FirebaseAuth.getInstance();
        mFirebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        currentUser = UserManager.getInstance().getUser();
        adapter = new BlockAdapter(blockContacts, this, this);
        rvListBlock.setAdapter(adapter);
        rvListBlock.setLayoutManager(mLinearLayoutManager);
        observeBlocks();
    }

    private void observeBlocks() {
        observeBlockEvent = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String blockID = dataSnapshot.getKey();
                userRepository.getUser(blockID, (error, data) -> {
                    if (error == null) {
                        User blockContact = (User) data[0];
                        adapter.addContact(blockContact);
                        blockIds.add(blockID);
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mBlockDatabase = mDatabase.child("users").child(mFirebaseUser.getUid()).child("blocks");
        mBlockDatabase.addChildEventListener(observeBlockEvent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                exit();
                break;
            case R.id.block_save:
                save();
                break;
            case R.id.block_add:
                add();
                break;
        }
    }


    private void save() {
        finish();
    }

    private void add() {
        Intent i = new Intent(this, SelectContactActivity.class);
        i.putExtra("SELECTED_ID", StringUtils.join(blockIds, ","));
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST);
    }

    private void blockContact(List<User> contacts) {
        for (User contact : contacts) {
            userRepository.toggleBlockUser(contact.key, true, null);
        }
    }
}
