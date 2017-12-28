package com.ping.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.ping.android.activity.ChatActivity;
import com.ping.android.activity.GroupProfileActivity;
import com.ping.android.activity.MainActivity;
import com.ping.android.activity.NewChatActivity;
import com.ping.android.activity.R;
import com.ping.android.activity.UserDetailActivity;
import com.ping.android.adapter.MessageAdapter;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends BaseFragment implements View.OnClickListener, MessageAdapter.ConversationItemListener {

    private final String TAG = "Ping: " + this.getClass().getSimpleName();

    private LinearLayoutManager linearLayoutManager;
    private SearchView searchView;
    private RecyclerView listChat;
    private Button btnDeleteMessage, btnEditMessage;
    private ImageView btnNewMessage;
    private User currentUser;
    private MessageAdapter adapter;
    private ArrayList<Conversation> conversations;
    private boolean isEditMode;

    private UserRepository userRepository;
    private ConversationRepository conversationRepository;

    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        bindViews(view);
        init();
        bindData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUnreadNumber();
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

    private void init() {
        userRepository = new UserRepository();
        conversationRepository = new ConversationRepository();
        currentUser = UserManager.getInstance().getUser();
        conversations = new ArrayList<>();
        adapter = new MessageAdapter(conversations);
        adapter.setListener(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Load data
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
        searchView.setOnClickListener(v -> searchView.setIconified(false));
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
        ChildEventListener observeConversationEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Conversation conversation = Conversation.from(dataSnapshot);
                if (MapUtils.isEmpty(conversation.memberIDs)) {
                    return;
                }
                insertOrUpdateMessage(conversation, true);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Conversation conversation = Conversation.from(dataSnapshot);
                if (MapUtils.isEmpty(conversation.memberIDs)) {
                    return;
                }
                insertOrUpdateMessage(conversation, false);
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
        DatabaseReference conversationRef = userRepository.getDatabaseReference()
                .child(currentUser.key)
                .child("conversations");
        conversationRef.orderByChild("timesstamps").addChildEventListener(observeConversationEvent);
        databaseReferences.put(conversationRef, observeConversationEvent);

        ChildEventListener groupEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Group group = Group.from(dataSnapshot);
                adapter.updateGroupConversation(group);
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
        DatabaseReference groupRef = userRepository.getDatabaseReference()
                .child(currentUser.key)
                .child("groups");
        groupRef.addChildEventListener(groupEventListener);
        databaseReferences.put(groupRef, groupEventListener);
    }

    private void insertOrUpdateMessage(Conversation conversation, Boolean isAddNew) {
        if(ServiceManager.getInstance().getDeleteStatusConversation(conversation)) {
            if (!isAddNew) {
                adapter.deleteConversation(conversation.key);
            }
            return;
        }

        userRepository.initMemberList(conversation.memberIDs, (error, data) -> {
            conversation.members = (List<User>) data[0];
            for (User user : conversation.members) {
                if (!user.key.equals(currentUser.key)) {
                    conversation.opponentUser = user;
                    break;
                }
            }
            if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                adapter.addOrUpdateConversation(conversation);
                updateUnreadNumber();
                scrollToTop();
            } else {
                ServiceManager.getInstance().getGroup(conversation.groupID, (error1, data1) -> {
                    if (error == null && data1.length > 0) {
                        conversation.group = (Group) data1[0];
                        adapter.addOrUpdateConversation(conversation);
                        updateUnreadNumber();
                        scrollToTop();
                    }
                });
            }
        });
    }

    private void scrollToTop() {
        listChat.scrollToPosition(0);
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
        showLoading();
        conversationRepository.deleteConversations(readConversations, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                hideLoading();
            }
        });
        adapter.cleanSelectConversation();
        isEditMode = false;
        updateEditMode();
    }

    @Override
    public void onOpenUserProfile(Conversation conversation, Pair<View, String>... sharedElements) {
        Intent intent = new Intent(getContext(), UserDetailActivity.class);
        intent.putExtra(Constant.START_ACTIVITY_USER_ID, conversation.opponentUser.key);
        intent.putExtra(UserDetailActivity.EXTRA_USER, conversation.opponentUser);
        intent.putExtra(UserDetailActivity.EXTRA_USER_IMAGE, sharedElements[0].second);
        intent.putExtra(UserDetailActivity.EXTRA_USER_NAME, sharedElements[1].second);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                sharedElements
        );
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onOpenGroupProfile(Conversation conversation, Pair<View, String>... sharedElements) {
        Intent intent = new Intent(getContext(), GroupProfileActivity.class);
        intent.putExtra(Constant.START_ACTIVITY_GROUP_ID, conversation.groupID);
        intent.putExtra(GroupProfileActivity.EXTRA_IMAGE_KEY, sharedElements[0].second);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                sharedElements
        );
        startActivityForResult(intent, 123, options.toBundle());
    }

    @Override
    public void onOpenChatScreen(Conversation conversation, List<Pair<View, String>> sharedElement) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("CONVERSATION", conversation);
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversation.key);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onSelect(Conversation conversation) {
        updateEditMenu();
    }

    private void updateUnreadNumber() {
        int unread = adapter.unreadNum();
        prefs.edit().putInt(Constant.PREFS_KEY_MESSAGE_COUNT, unread).apply();
    }
}