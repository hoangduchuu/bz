package com.ping.android.presentation.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ping.android.presentation.view.activity.AddContactActivity;
import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.R;
import com.ping.android.dagger.loggedin.main.MainComponent;
import com.ping.android.dagger.loggedin.main.contact.ContactComponent;
import com.ping.android.dagger.loggedin.main.contact.ContactModule;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ContactPresenter;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.presentation.view.activity.UserDetailActivity;
import com.ping.android.presentation.view.adapter.ContactAdapter;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Constant;

import javax.inject.Inject;

public class ContactFragment extends BaseFragment
        implements View.OnClickListener, ContactAdapter.ClickListener, ContactPresenter.View {
    private RecyclerView rvListContact;
    private LinearLayoutManager mLinearLayoutManager;
    private SearchView searchView;
    private ContactAdapter adapter;

    @Inject
    ContactPresenter presenter;
    ContactComponent component;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        component().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        bindViews(view);
        init();
        bindData();
        presenter.create();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
    public ContactPresenter getPresenter() {
        return presenter;
    }

    @Override
    public void onSendMessage(User contact) {
        if (!ServiceManager.getInstance().getNetworkStatus(getContext())) {
            Toast.makeText(getContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        presenter.handleSendMessage(contact);
    }

    @Override
    public void onVoiceCall(User contact) {
        presenter.handleVoiceCallPress(contact);
    }

    @Override
    public void onVideoCall(User contact) {
        presenter.handleVideoCallPress(contact);
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
        rvListContact = view.findViewById(R.id.contact_recycle_view);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        searchView = view.findViewById(R.id.contact_search_view);
    }

    private void init() {
        adapter = new ContactAdapter(getActivity(), this);
        //observeContacts();
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

    private void onAddContact(View view) {
        if (!ServiceManager.getInstance().getNetworkStatus(getContext())) {
            Toast.makeText(getContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getActivity(), AddContactActivity.class);
        startActivity(intent);
    }

    private ContactComponent component() {
        if (component == null) {
            component = getComponent(MainComponent.class)
                    .provideContactComponent(new ContactModule(this));
        }
        return component;
    }

    @Override
    public void addFriend(User data) {
        adapter.addContact(data);
    }

    @Override
    public void removeFriend(String key) {
        adapter.deleteContact(key);
    }

    @Override
    public void openConversation(String conversationId) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
        startActivity(intent);
    }

    @Override
    public void openCallScreen(User currentUser, User otherUser, boolean isVideo) {
        CallActivity.start(getContext(), currentUser, otherUser, isVideo);
    }
}
