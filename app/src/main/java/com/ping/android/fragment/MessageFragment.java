package com.ping.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.activity.MainActivity;
import com.ping.android.activity.NewChatActivity;
import com.ping.android.activity.R;
import com.ping.android.adapter.MessageAdapter;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment implements View.OnClickListener, MessageAdapter.ClickListener {

    private final String TAG = "Ping: " + this.getClass().getSimpleName();
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase, mMessageDatabase;
    private ChildEventListener observeConversationEvent;
    private ChildEventListener groupEventListener;
    private LinearLayoutManager linearLayoutManager;
    private SearchView searchView;
    private RecyclerView listChat;
    private Button btnDeleteMessage, btnEditMessage;
    private ImageView btnNewMessage;
    private User currentUser;
    private MessageAdapter adapter;
    private ArrayList<Conversation> conversations;
    private boolean loadData, loadGUI, isEditMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServiceManager.getInstance().initUserData(new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                init();
                loadData = true;
                if (loadGUI) {
                    bindData();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
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
        if (mMessageDatabase != null) {
            mMessageDatabase.removeEventListener(observeConversationEvent);
        }
        if (groupEventListener != null) {
            ServiceManager.getInstance().stopListenGroupChange(currentUser.key, groupEventListener);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.message_add:
                onNewChat();
                break;
            case R.id.message_edit:
                onEdit();
                break;
            case R.id.message_delete:
                onDelete();
                break;
        }
    }

    @Override
    public void onSelect(ArrayList<Conversation> selectConversations) {
        updateEditMode();
    }

    private void init() {
        currentUser = ServiceManager.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        conversations = new ArrayList<>();
        adapter = new MessageAdapter(conversations, getActivity(), this);
        observeMessages();
    }

    private void bindViews(View view) {
        listChat = (RecyclerView) view.findViewById(R.id.message_recycle_view);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        searchView = (SearchView) view.findViewById(R.id.message_search_view);
        CommonMethod.UpdateSearchViewLayout(searchView);
        btnEditMessage = (Button) view.findViewById(R.id.message_edit);
        btnEditMessage.setOnClickListener(this);
        btnDeleteMessage = (Button) view.findViewById(R.id.message_delete);
        btnDeleteMessage.setOnClickListener(this);
        btnNewMessage = (ImageView) view.findViewById(R.id.message_add);
        btnNewMessage.setOnClickListener(this);
    }

    private void bindData() {
        listChat.setLayoutManager(linearLayoutManager);
        listChat.setAdapter(adapter);

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
        updateEditMode();
    }

    private void updateEditMode() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.onEditMode(isEditMode);
        adapter.setEditMode(isEditMode);
        if (isEditMode) {
            btnEditMessage.setText("CANCEL");
            btnNewMessage.setVisibility(View.GONE);
            btnDeleteMessage.setVisibility(View.VISIBLE);
        } else {
            btnEditMessage.setText("EDIT");
            btnNewMessage.setVisibility(View.VISIBLE);
            btnDeleteMessage.setVisibility(View.GONE);
        }

        updateEditMenu();
    }

    private void updateEditMenu() {
        if (CollectionUtils.isEmpty(adapter.getSelectConversation())) {
            btnDeleteMessage.setEnabled(false);
        } else {
            btnDeleteMessage.setEnabled(true);
        }
    }

    private void observeMessages() {
        observeConversationEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                insertOrUpdateMessage(dataSnapshot, true);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                insertOrUpdateMessage(dataSnapshot, false);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String conversationID = dataSnapshot.getKey();
                adapter.deleteConversation(conversationID);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mMessageDatabase = mDatabase.child("users").child(auth.getCurrentUser().getUid()).child("conversations");
        mMessageDatabase.addChildEventListener(observeConversationEvent);

        groupEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Group group = Group.from(dataSnapshot);
                adapter.onGroupChange(group);
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
        ServiceManager.getInstance().listenGroupChange(currentUser.key, groupEventListener);
    }

    private void insertOrUpdateMessage(DataSnapshot dataSnapshot, Boolean isAddNew) {
        Conversation conversation = Conversation.from(dataSnapshot);
        if (MapUtils.isEmpty(conversation.memberIDs)) {
            return;
        }
        if(ServiceManager.getInstance().getCurrentDeleteStatus(conversation.deleteStatuses)) {
            if (!isAddNew) {
                adapter.deleteConversation(conversation.key);
            }
            return;
        }

        ServiceManager.getInstance().initMembers(conversation.memberIDs, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                conversation.members = (List<User>) data[0];
                for (User user : conversation.members) {
                    if (!user.key.equals(currentUser.key)) {
                        conversation.opponentUser = user;
                        break;
                    }
                }
                if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                    adapter.addOrUpdateConversation(conversation);
                } else {
                    ServiceManager.getInstance().getGroup(conversation.groupID, new Callback() {
                        @Override
                        public void complete(Object error, Object... data) {
                            conversation.group = (Group) data[0];
                            adapter.addOrUpdateConversation(conversation);
                        }
                    });
                }
            }
        });
    }

    private void onNewChat() {
        if (!ServiceManager.getInstance().getNetworkStatus(getContext())) {
            Toast.makeText(getContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getActivity(), NewChatActivity.class);
        getActivity().startActivity(intent);
    }

    private void onEdit() {
        isEditMode = !isEditMode;
        updateEditMode();
    }

    private void onRead() {
        ArrayList<Conversation> unreadConversations = new ArrayList<>();
        for (Conversation conversation : conversations) {
            Boolean readStatus = conversation.readStatuses.get(currentUser.key);
            if (!readStatus) {
                unreadConversations.add(conversation);
            }
        }
        ServiceManager.getInstance().updateConversationReadStatus(unreadConversations, true);
        isEditMode = false;
        updateEditMode();
    }

    private void onDelete() {
        ArrayList<Conversation> readConversations = new ArrayList<>(adapter.getSelectConversation());
        ServiceManager.getInstance().deleteConversation(readConversations);
        for (Conversation conversation : readConversations) {
            adapter.deleteConversation(conversation.key);
        }
        adapter.cleanSelectConversation();
        isEditMode = false;
        updateEditMode();
    }
}