package com.ping.android.presentation.view.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.activity.MainActivity;
import com.ping.android.dagger.loggedin.main.MainComponent;
import com.ping.android.dagger.loggedin.main.conversation.ConversationComponent;
import com.ping.android.dagger.loggedin.main.conversation.ConversationModule;
import com.ping.android.fragment.BaseFragment;
import com.ping.android.presentation.view.activity.ConversationDetailActivity;
import com.ping.android.presentation.view.activity.NewChatActivity;
import com.ping.android.activity.R;
import com.ping.android.activity.UserDetailActivity;
import com.ping.android.presentation.view.adapter.MessageAdapter;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ConversationListPresenter;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.bus.BusProvider;
import com.ping.android.utils.bus.events.TransphabetEvent;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ConversationFragment extends BaseFragment implements View.OnClickListener, MessageAdapter.ConversationItemListener, ConversationListPresenter.View {

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

    private ConversationRepository conversationRepository;

    private SharedPreferences prefs;

    @Inject
    ConversationListPresenter presenter;
    @Inject
    BusProvider busProvider;
    ConversationComponent component;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        component().inject(this);
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
        conversationRepository = new ConversationRepository();
        currentUser = UserManager.getInstance().getUser();
        conversations = new ArrayList<>();
        adapter = new MessageAdapter(conversations);
        adapter.setListener(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        presenter.getConversations();
        // Load data
        //observeMessages();
    }

    private void bindViews(View view) {
        listChat = (RecyclerView) view.findViewById(R.id.message_recycle_view);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        searchView = view.findViewById(R.id.message_search_view);
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
        Intent intent = new Intent(getContext(), ConversationDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString(ConversationDetailActivity.CONVERSATION_KEY, conversation.key);
        intent.putExtra(ConversationDetailActivity.EXTRA_IMAGE_KEY, sharedElements[0].second);
        intent.putExtras(extras);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                sharedElements
        );
        startActivity(intent, options.toBundle());
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

    public ConversationComponent component() {
        if (component == null) {
            component = getComponent(MainComponent.class).provideConversationComponent(new ConversationModule(this));
        }
        return component;
    }

    @Override
    public void addConversation(Conversation conversation) {
        adapter.updateConversation(conversation);
        scrollToTop();
    }

    @Override
    public void updateConversation(Conversation conversation) {
        adapter.updateConversation(conversation);
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
        adapter.updateData(conversations);
    }

    private void listenTransphabetChanged() {
        registerEvent(busProvider.getEvents()
                .subscribe(object -> {
                    if (object instanceof TransphabetEvent) {
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }));
    }
}