package com.ping.android.presentation.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bzzzchat.cleanarchitecture.UIThread;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.ping.android.R;
import com.ping.android.dagger.loggedin.SearchUserModule;
import com.ping.android.dagger.loggedin.addcontact.AddContactComponent;
import com.ping.android.dagger.loggedin.addcontact.AddContactModule;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.AddContactPresenter;
import com.ping.android.presentation.presenters.SearchUserPresenter;
import com.ping.android.presentation.view.adapter.AddContactAdapter;
import com.ping.android.utils.configs.Constant;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class AddContactActivity extends CoreActivity implements AddContactAdapter.ClickListener, View.OnClickListener, SearchUserPresenter.View, AddContactPresenter.View {
    private RecyclerView rvListContact;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText searchView;
    private ImageView btBack;
    private AVLoadingIndicatorView avi;

    private AddContactAdapter adapter;
    private LinearLayout noResultsView;

    @Inject
    public SearchUserPresenter searchUserPresenter;
    @Inject
    public AddContactPresenter presenter;
    private AddContactComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        getComponent().inject(this);
        searchUserPresenter.create();
        bindViews();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchUserPresenter.destroy();
        presenter.destroy();
    }

    private void bindViews() {
        btBack = findViewById(R.id.add_contact_back);
        btBack.setOnClickListener(this);
        rvListContact = findViewById(R.id.add_contact_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(false);
        searchView = findViewById(R.id.add_contact_search_view);
        noResultsView = findViewById(R.id.no_results);
        avi = findViewById(R.id.avi);

        registerEvent(RxTextView.textChanges(searchView)
                .debounce(300, TimeUnit.MILLISECONDS)
                .observeOn(new UIThread().getScheduler())
                .subscribe(searchViewQueryTextEvent -> searchUserPresenter.searchUsers(searchViewQueryTextEvent.toString())));
    }

    private void init() {
        adapter = new AddContactAdapter(new ArrayList<>(), this);
        rvListContact.setAdapter(adapter);
        rvListContact.setLayoutManager(mLinearLayoutManager);
    }

    @Override
    public void onAddFriend(User contact) {
        if (networkStatus != Constant.NETWORK_STATUS.CONNECTED) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        presenter.addContact(contact.key);
        contact.typeFriend = Constant.TYPE_FRIEND.IS_FRIEND;
    }

    @Override
    public void onSendMessage(User contact) {
        if (networkStatus != Constant.NETWORK_STATUS.CONNECTED) {
            Toast.makeText(AddContactActivity.this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        presenter.createPVPConversation(contact);
    }

    @Override
    public void onViewProfile(User contact, Pair<View, String>[] sharedElements) {
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra(Constant.START_ACTIVITY_USER_ID, contact.key);
        intent.putExtra(UserDetailActivity.EXTRA_USER, contact);
        intent.putExtra(UserDetailActivity.EXTRA_USER_IMAGE, sharedElements[0].second);
        intent.putExtra(UserDetailActivity.EXTRA_USER_NAME, sharedElements[1].second);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                sharedElements
        );
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_contact_back:
                onExitAddContact();
                break;
        }
    }

    private void onExitAddContact() {
        finish();
    }

    public AddContactComponent getComponent() {
        if (component == null) {
            component = getLoggedInComponent()
                    .provideAddContactComponent(new AddContactModule(this), new SearchUserModule(this));
        }
        return component;
    }

    @Override
    public void displaySearchResult(List<User> users) {
        adapter.updateData(new ArrayList<>(users));
    }

    @Override
    public void showNoResults() {
        rvListContact.post(()-> rvListContact.setVisibility(View.GONE));
        noResultsView.post(() -> noResultsView.setVisibility(View.VISIBLE));
    }

    @Override
    public void hideNoResults() {
        rvListContact.post(()->rvListContact.setVisibility(View.VISIBLE));
        noResultsView.post(() -> noResultsView.setVisibility(View.GONE));
    }

    @Override
    public void showSearching() {
        avi.post(() -> {
            avi.setVisibility(View.VISIBLE);
            avi.show();
        });
    }

    @Override
    public void hideSearching() {
        avi.post(() -> {
            avi.hide();
            avi.setVisibility(View.GONE);
        });
    }

    @Override
    public void moveToChatScreen(String s) {
        Intent intent = new Intent(AddContactActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, s);
        startActivity(intent);
    }
}
