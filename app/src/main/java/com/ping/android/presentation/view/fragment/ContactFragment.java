package com.ping.android.presentation.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.ping.android.R;
import com.ping.android.data.repository.TutorialHelper;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ContactPresenter;
import com.ping.android.presentation.view.activity.AddContactActivity;
import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.presentation.view.activity.UserDetailActivity;
import com.ping.android.presentation.view.adapter.ContactAdapter;
import com.ping.android.utils.configs.Constant;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class ContactFragment extends BaseFragment
        implements View.OnClickListener, ContactAdapter.ClickListener, ContactPresenter.View {
    private RecyclerView rvListContact;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText searchView;
    private ContactAdapter adapter;
    private SpinKitView tutoView;

    @Inject
    ContactPresenter presenter;

    @Inject
    TutorialHelper tutorialHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
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
    public void onResume() {
        super.onResume();
        setupTutorial();
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
                if (!tutorialHelper.isTutorial03AddFriendCLicked()){
                    tutorialHelper.markTutorial03AddFriendCLicked();
                }
                break;
        }
    }

    @Override
    public ContactPresenter getPresenter() {
        return presenter;
    }

    @Override
    public void onSendMessage(User contact) {
        if (!isNetworkAvailable()) {
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
        tutoView = view.findViewById(R.id.tutorial_dot_3);
        setupTutorial();
    }

    private void init() {
        adapter = new ContactAdapter(getActivity(), this);
        //observeContacts();
    }

    private void bindData() {
        rvListContact.setAdapter(adapter);
        rvListContact.setLayoutManager(mLinearLayoutManager);

        registerEvent(RxTextView.textChanges(searchView)
        .subscribe(charSequence -> adapter.filter(charSequence.toString())));
    }

    private void onAddContact(View view) {
        if (!isNetworkAvailable()) {
            Toast.makeText(getContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getActivity(), AddContactActivity.class);
        startActivity(intent);
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


    private void setupTutorial() {
        if (!tutorialHelper.isTutorial03AddFriendCLicked()){
            tutoView.setVisibility(View.VISIBLE);
        }else {
            tutoView.setVisibility(View.GONE);

        }
    }
}
