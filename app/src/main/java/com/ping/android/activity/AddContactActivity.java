package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.adapter.AddContactAdapter;
import com.ping.android.adapter.SelectContactAdapter;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.Log;
import com.ping.android.utils.Toaster;

import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AddContactActivity extends CoreActivity implements AddContactAdapter.ClickListener, View.OnClickListener {
    private static final int DELAY = 300;

    private RecyclerView rvListContact;
    private LinearLayoutManager mLinearLayoutManager;
    private SearchView searchView;
    private ImageView btBack;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private User currentUser;
    private AddContactAdapter adapter;

    private UserRepository userRepository;
    private ArrayList<User> allUsers = new ArrayList<>();
    private Map<String, User> userList = new HashMap<>();
    private Timer timer;
    private String textToSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        bindViews();
        init();
    }

    private void bindViews() {
        btBack = (ImageView) findViewById(R.id.add_contact_back);
        btBack.setOnClickListener(this);
        rvListContact = (RecyclerView) findViewById(R.id.add_contact_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(false);
        searchView = (SearchView) findViewById(R.id.add_contact_search_view);
        CommonMethod.UpdateSearchViewLayout(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (timer != null) {
                    timer.cancel();
                }
                textToSearch = newText;
                timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        // Trigger task after delaying
                        searchUsers(textToSearch);
                    }
                };
                timer.schedule(task, DELAY);
                return true;
            }
        });
        searchView.setOnClickListener(v -> searchView.setIconified(false));
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        userRepository = new UserRepository();

        currentUser = UserManager.getInstance().getUser();

        adapter = new AddContactAdapter(this, new ArrayList<>(), this);
        rvListContact.setAdapter(adapter);
        rvListContact.setLayoutManager(mLinearLayoutManager);
        adapter.filter("");

        loadAllUsers();
    }

    private void loadAllUsers() {
        showLoading();
        userRepository.getDatabaseReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                User user = new User(snapshot);
                                updateTypeFriend(user);
                                allUsers.add(user);
                            }
                            adapter.updateData(allUsers);
                            hideLoading();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        hideLoading();
                        Toaster.longToast("Could not load users data");
                    }
                });
    }

    @Override
    public void onAddFriend(User contact) {
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        ServiceManager.getInstance().addContact(contact);
        contact.typeFriend = Constant.TYPE_FRIEND.IS_FRIEND;
    }

    @Override
    public void onSendMessage(User contact) {
        if (!ServiceManager.getInstance().getNetworkStatus(AddContactActivity.this)) {
            Toast.makeText(AddContactActivity.this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        ServiceManager.getInstance().createConversationIDForPVPChat(currentUser.key, contact.key,
                (error, data) -> {
                    String conversationID = data[0].toString();
                    Intent intent = new Intent(AddContactActivity.this, ChatActivity.class);
                    intent.putExtra(ChatActivity.CONVERSATION_ID, conversationID);
                    startActivity(intent);
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_contact_back:
                onExitAddContact();
                break;
        }
    }

    public void searchUsers(String text) {
        rvListContact.post(() -> adapter.filter(text));
    }

    private void updateTypeFriend(User user) {
        user.typeFriend = Constant.TYPE_FRIEND.NON_FRIEND;
        if (currentUser != null && MapUtils.isNotEmpty(currentUser.friends)) {
            Boolean isFriend = currentUser.friends.get(user.key);
            if (isFriend != null && isFriend) {
                user.typeFriend = Constant.TYPE_FRIEND.IS_FRIEND;
            }
        }
    }

    private void onExitAddContact() {
        finish();
    }
}
