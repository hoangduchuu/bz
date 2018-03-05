package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.widget.RxSearchView;
import com.ping.android.presentation.view.adapter.AddContactAdapter;
import com.ping.android.dagger.loggedin.SearchUserModule;
import com.ping.android.dagger.loggedin.addcontact.AddContactComponent;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.SearchUserPresenter;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.bzzzchat.cleanarchitecture.UIThread;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class AddContactActivity extends CoreActivity implements AddContactAdapter.ClickListener, View.OnClickListener, SearchUserPresenter.View {
    private static final int DELAY = 300;

    private RecyclerView rvListContact;
    private LinearLayoutManager mLinearLayoutManager;
    private SearchView searchView;
    private ImageView btBack;
    private AVLoadingIndicatorView avi;

    private User currentUser;
    private AddContactAdapter adapter;
    private LinearLayout noResultsView;

    private Timer timer;
    private String textToSearch = "";

    @Inject
    public SearchUserPresenter searchUserPresenter;
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

        CommonMethod.UpdateSearchViewLayout(searchView);
        registerEvent(RxSearchView.queryTextChangeEvents(searchView)
                .debounce(300, TimeUnit.MILLISECONDS)
                .observeOn(new UIThread().getScheduler())
                .subscribe(searchViewQueryTextEvent -> searchUserPresenter.searchUsers(searchViewQueryTextEvent.queryText().toString())));
        searchView.setOnClickListener(v -> searchView.setIconified(false));
    }

    private void init() {
        currentUser = UserManager.getInstance().getUser();

        adapter = new AddContactAdapter(new ArrayList<>(), this);
        rvListContact.setAdapter(adapter);
        rvListContact.setLayoutManager(mLinearLayoutManager);
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

    public void searchUsers(String text) {
        searchUserPresenter.searchUsers(text);
    }

    private void onExitAddContact() {
        finish();
    }

    public AddContactComponent getComponent() {
        if (component == null) {
            component = getLoggedInComponent()
                    .provideAddContactComponent(new SearchUserModule(this));
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
}
