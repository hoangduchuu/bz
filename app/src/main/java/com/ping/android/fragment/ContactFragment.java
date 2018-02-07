package com.ping.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.activity.AddContactActivity;
import com.ping.android.activity.CallActivity;
import com.ping.android.activity.ChatActivity;
import com.ping.android.activity.R;
import com.ping.android.activity.UserDetailActivity;
import com.ping.android.adapter.ContactAdapter;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

public class ContactFragment extends Fragment implements View.OnClickListener, ContactAdapter.ClickListener {

    private RecyclerView rvListContact;
    private LinearLayoutManager mLinearLayoutManager;
    private SearchView searchView;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private DatabaseReference mContactDatabase;
    private User currentUser;
    private ContactAdapter adapter;
    private boolean loadData, loadGUI;
    private ChildEventListener observeContactEvent;

    private UserRepository userRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        loadData = true;
        if (loadGUI) {
            bindData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        bindViews(view);
        if (loadData & !loadGUI) {
            bindData();
        }
        loadGUI = true;
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        loadGUI = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mContactDatabase != null) {
            mContactDatabase.removeEventListener(observeContactEvent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.contact_add:
                onAddContact(view);
                break;
        }
    }

    @Override
    public void onSendMessage(User contact) {
        if (!ServiceManager.getInstance().getNetworkStatus(getContext())) {
            Toast.makeText(getContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        ServiceManager.getInstance().createConversationIDForPVPChat(currentUser.key, contact.key,
                new Callback() {
                    @Override
                    public void complete(Object error, Object... data) {
                        String conversationID = data[0].toString();
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationID);
                        startActivity(intent);
                    }
                });
    }

    @Override
    public void onVoiceCall(User contact) {
        CallActivity.start(getContext(), contact, false);
    }

    @Override
    public void onVideoCall(User contact) {
        CallActivity.start(getContext(), contact, true);
    }

    @Override
    public void onOpenProfile(User contact, Pair<View, String>... sharedElements) {
        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        intent.putExtra(Constant.START_ACTIVITY_USER_ID, contact.key);
        intent.putExtra(UserDetailActivity.EXTRA_USER, contact);
        intent.putExtra(UserDetailActivity.EXTRA_USER_IMAGE, sharedElements[0].second);
        intent.putExtra(UserDetailActivity.EXTRA_USER_NAME, sharedElements[1].second);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                sharedElements
        );
        startActivity(intent, options.toBundle());
    }

    private void bindViews(View view) {
        view.findViewById(R.id.contact_add).setOnClickListener(this);
        rvListContact = (RecyclerView) view.findViewById(R.id.contact_recycle_view);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        searchView = (SearchView) view.findViewById(R.id.contact_search_view);
        CommonMethod.UpdateSearchViewLayout(searchView);
    }

    private void init() {
        userRepository = new UserRepository();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        currentUser = UserManager.getInstance().getUser();
        adapter = new ContactAdapter(getActivity(), this);
        observeContacts();
    }

    private void bindData() {
        rvListContact.setAdapter(adapter);
        rvListContact.setLayoutManager(mLinearLayoutManager);

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

    private void observeContacts() {
        observeContactEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String contactID = dataSnapshot.getKey();
                Boolean exist = Boolean.valueOf(dataSnapshot.getValue().toString());
                if (exist) {
                    userRepository.getUser(contactID, (error, data) -> {
                        if (error == null) {
                            User contact = (User) data[0];
                            adapter.addContact(contact);
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String contactID = dataSnapshot.getKey();
                Boolean exist = Boolean.valueOf(dataSnapshot.getValue().toString());
                if (exist) {
                    userRepository.getUser(contactID, (error, data) -> {
                        if (error == null) {
                            User contact = (User) data[0];
                            adapter.updateContact(contact);
                        }
                    });
                } else {
                    adapter.deleteContact(contactID);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String contactID = dataSnapshot.getKey();
                adapter.deleteContact(contactID);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mContactDatabase = mDatabase.child("friends").child(currentUser.key);
        mContactDatabase.addChildEventListener(observeContactEvent);
    }

    private void onAddContact(View view) {
        if (!ServiceManager.getInstance().getNetworkStatus(getContext())) {
            Toast.makeText(getContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getActivity(), AddContactActivity.class);
        startActivity(intent);
    }
}
