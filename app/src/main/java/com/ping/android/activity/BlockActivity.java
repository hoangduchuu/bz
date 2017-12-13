package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.ping.android.adapter.BlockAdapter;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Constant;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BlockActivity extends CoreActivity implements View.OnClickListener, BlockAdapter.ClickListener {

    private final String TAG = "Ping: " + this.getClass().getSimpleName();
    private ChildEventListener observeBlockEvent;

    private RecyclerView rvListBlock;
    private LinearLayoutManager mLinearLayoutManager;
    private Button actionButton;
    private LinearLayout bottomLayout;
    private TextView bottomText;

    private BlockAdapter adapter;
    private User currentUser;
    private List<String> blockIds = new ArrayList<>();

    private UserRepository userRepository;
    private DatabaseReference mBlockDatabase;

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
        actionButton = findViewById(R.id.block_save);
        actionButton.setOnClickListener(this);
        bottomLayout = findViewById(R.id.block_add);
        bottomLayout.setOnClickListener(this);
        bottomText = findViewById(R.id.bottom_text);
    }

    private void init() {
        userRepository = new UserRepository();
        currentUser = UserManager.getInstance().getUser();
        adapter = new BlockAdapter(new ArrayList<>(), this, this);
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
                        actionButton.setEnabled(true);
                        blockIds.add(blockID);
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String blockID = dataSnapshot.getKey();
                adapter.removeContact(blockID);
                if (adapter.getItemCount() == 0) {
                    actionButton.setEnabled(false);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mBlockDatabase = userRepository.getDatabaseReference().child(currentUser.key).child("blocks");
        mBlockDatabase.addChildEventListener(observeBlockEvent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                exit();
                break;
            case R.id.block_save:
                onEditPress();
                break;
            case R.id.block_add:
                if (adapter.isInEditMode()) {
                    unBlockContact(adapter.getSelectedContact());
                    rvListBlock.postDelayed(this::onEditPress, 500);
                } else {
                    add();
                }
                break;
        }
    }


    private void onEditPress() {
        adapter.toggleEditMode();
        this.refreshView(adapter.isInEditMode());
    }

    private void refreshView(boolean isEditMode) {
        actionButton.setText(isEditMode ? "CANCEL" : "EDIT");
        bottomText.setText(isEditMode ? "UNBLOCK" : "ADD NEW");

    }

    private void add() {
        Intent i = new Intent(this, SelectContactActivity.class);
        i.putExtra("SELECTED_ID", StringUtils.join(blockIds, ","));
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST);
    }

    private void blockContact(List<User> contacts) {
        for (User contact : contacts) {
            userRepository.toggleBlockUser(contact.key, true);
        }
    }

    private void unBlockContact(List<User> contacts) {
        for (User contact : contacts) {
            userRepository.toggleBlockUser(contact.key, false);
        }
    }
}
