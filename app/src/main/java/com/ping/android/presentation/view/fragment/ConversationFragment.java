package com.ping.android.presentation.view.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.ping.android.R;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.enums.NetworkStatus;
import com.ping.android.presentation.presenters.ConversationListPresenter;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.presentation.view.activity.ConversationDetailActivity;
import com.ping.android.presentation.view.activity.CoreActivity;
import com.ping.android.presentation.view.activity.MainActivity;
import com.ping.android.presentation.view.activity.NewChatActivity;
import com.ping.android.presentation.view.activity.UserDetailActivity;
import com.ping.android.presentation.view.adapter.MessageAdapter;
import com.ping.android.utils.bus.BusProvider;
import com.ping.android.utils.bus.events.ConversationChangeEvent;
import com.ping.android.utils.configs.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class ConversationFragment extends BaseFragment implements View.OnClickListener,
        MessageAdapter.ConversationItemListener, ConversationListPresenter.View {

    private final String TAG = "Ping: " + this.getClass().getSimpleName();

    private LinearLayoutManager linearLayoutManager;
    private EditText searchEdt;
    private RecyclerView listChat;
    private Button btnDeleteMessage;
    private TextView btnEditMessage;
    private ImageView btnNewMessage;
    private MessageAdapter adapter;
    private boolean isEditMode;
    //private boolean isScrollToTop = false;

    private SharedPreferences prefs;

    @Inject
    ConversationListPresenter presenter;
    @Inject
    BusProvider busProvider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
        presenter.create();
        listenTransphabetChanged();
    }

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
    public void onDestroy() {
        super.onDestroy();
        presenter.destroy();
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
        adapter = new MessageAdapter();
        adapter.setListener(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        presenter.getConversations();
        // Load data
        //observeMessages();
    }

    private void bindViews(View view) {
        listChat = view.findViewById(R.id.message_recycle_view);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        //searchView = view.findViewById(R.id.message_search_view);
        searchEdt = view.findViewById(R.id.search_edt);
        btnEditMessage = view.findViewById(R.id.message_edit);
        btnEditMessage.setOnClickListener(this);
        btnDeleteMessage = view.findViewById(R.id.message_delete);
        btnDeleteMessage.setOnClickListener(this);
        btnNewMessage = view.findViewById(R.id.message_add);
        btnNewMessage.setOnClickListener(this);
    }

    private void bindData() {
        listChat.setLayoutManager(linearLayoutManager);
        listChat.setAdapter(adapter);
        // FIXME: Enable load more feature but affect to search function
        /*listChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager
                        .findLastVisibleItemPosition();
                if (totalItemCount <= (lastVisibleItem + 5)) {
                    presenter.loadMore();
                }
            }
        });*/
        registerEvent(RxTextView.textChanges(searchEdt)
                .subscribe(charSequence -> {
                    adapter.filter(charSequence.toString());
                    /*if (charSequence.length() > 0) {
                        // Show X icon
                        searchEdt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, R.drawable.ic_search_close, 0);
                    } else {
                        // Hide x icon
                        searchEdt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0);
                    }*/
                }));
        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        */
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
        if (adapter.getSelectConversation().size() == 0) {
            btnDeleteMessage.setEnabled(false);
        } else {
            btnDeleteMessage.setEnabled(true);
        }
    }

    private void scrollToTop() {
        listChat.post(() -> listChat.scrollToPosition(0));
    }

    private void onNewChat() {
        if (!isNetworkAvailable()) {
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

//    private void onRead() {
//        ArrayList<Conversation> unreadConversations = new ArrayList<>();
//        for (Conversation conversation : callList) {
//            Boolean readStatus = conversation.readStatuses.get(currentUser.key);
//            if (!readStatus) {
//                unreadConversations.add(conversation);
//            }
//        }
//        ServiceManager.getInstance().updateConversationReadStatus(unreadConversations, true);
//        isEditMode = false;
//        updateEditMode();
//    }

    private void onDelete() {
        ArrayList<Conversation> readConversations = new ArrayList<>(adapter.getSelectConversation());
        presenter.deleteConversations(readConversations);
        adapter.cleanSelectConversation();
        new Handler().postDelayed(() -> {
            isEditMode = false;
            updateEditMode();
        }, 500);
    }

    @Override
    public void onOpenUserProfile(Conversation conversation, Pair<View, String>... sharedElements) {
        Intent intent = new Intent(getContext(), UserDetailActivity.class);
        intent.putExtra(Constant.START_ACTIVITY_USER_ID, conversation.opponentUser.key);
        intent.putExtra(UserDetailActivity.EXTRA_USER, conversation.opponentUser);
        intent.putExtra(UserDetailActivity.EXTRA_USER_IMAGE, sharedElements[0].second);
        intent.putExtra(UserDetailActivity.EXTRA_USER_NAME, sharedElements[1].second);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, conversation.currentColor.getCode());
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                sharedElements
        );
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onOpenGroupProfile(Conversation conversation, Pair<View, String>... sharedElements) {
        Intent intent = new Intent(getContext(), ConversationDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString(ConversationDetailActivity.CONVERSATION_KEY, conversation.key);
        intent.putExtra(ConversationDetailActivity.EXTRA_IMAGE_KEY, sharedElements[0].second);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, conversation.currentColor.getCode());
        intent.putExtras(extras);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                sharedElements
        );
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onOpenChatScreen(Conversation conversation, Pair<View, String>... sharedElements) {
        //Bundle bundle = new Bundle();
        //bundle.putParcelable("CONVERSATION", conversation);
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversation.key);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_NAME, conversation.conversationName);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, conversation.currentColor.getCode());
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_TRANSITION_NAME, sharedElements[0].second);
        //intent.putExtras(bundle);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getActivity(),
                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
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

    @Override
    public void addConversation(Conversation conversation) {
        adapter.updateConversation(conversation, true);
        updateUnreadNumber();
        scrollToTop();
    }

    @Override
    public void updateConversation(Conversation conversation) {
        adapter.updateConversation(conversation, false);
        updateUnreadNumber();
        scrollToTop();
    }

    @Override
    public void deleteConversation(Conversation data) {
        adapter.deleteConversation(data.key);
    }

    @Override
    public void updateGroupConversation(Group data) {
        adapter.updateGroupConversation(data);
    }

    @Override
    public void updateConversationList(List<Conversation> conversations) {
        adapter.updateData(new ArrayList<>(conversations));
        updateUnreadNumber();
    }

    @Override
    public void notifyConversationChange(Conversation data) {
        ConversationChangeEvent event = new ConversationChangeEvent();
        event.conversationId = data.key;
        event.nickName = data.conversationName;
        busProvider.post(event);
    }

    @Override
    public void appendConversations(List<Conversation> conversations) {
        adapter.appendConversations(conversations);
        updateUnreadNumber();
    }

    @Override
    public void updateMappings(Map<String, String> mappings) {
        adapter.updateMappings(mappings);
    }

    @Override
    public void showConnecting() {
        if (getActivity() instanceof CoreActivity) {
            ((CoreActivity) getActivity()).connectivityChanged(NetworkStatus.CONNECTING);
        }
    }

    @Override
    public void hideConnecting() {
        if (getActivity() instanceof CoreActivity) {
            ((CoreActivity) getActivity()).connectivityChanged(NetworkStatus.CONNECTED);
        }
    }

    private void listenTransphabetChanged() {
//        registerEvent(busProvider.getEvents()
//                .subscribe(object -> {
//                    if (object instanceof TransphabetEvent) {
//                        if (adapter != null) {
//                            adapter.notifyDataSetChanged();
//                        }
//                    }
//                }));
    }
}