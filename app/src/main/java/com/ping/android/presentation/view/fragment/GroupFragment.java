package com.ping.android.presentation.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ping.android.dagger.loggedin.main.MainComponent;
import com.ping.android.dagger.loggedin.main.group.GroupComponent;
import com.ping.android.dagger.loggedin.main.group.GroupModule;
import com.ping.android.fragment.BaseFragment;
import com.ping.android.presentation.presenters.GroupPresenter;
import com.ping.android.presentation.view.activity.AddGroupActivity;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.presentation.view.activity.MainActivity;
import com.ping.android.activity.R;
import com.ping.android.presentation.view.adapter.GroupAdapter;
import com.ping.android.model.Group;
import com.ping.android.presentation.view.activity.ConversationDetailActivity;
import com.ping.android.service.ServiceManager;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;

import javax.inject.Inject;

public class GroupFragment extends BaseFragment implements View.OnClickListener, GroupAdapter.ClickListener, GroupPresenter.View {
    private RelativeLayout bottomMenu;
    private RecyclerView listGroup;
    private LinearLayoutManager linearLayoutManager;
    private SearchView searchView;
    private Button btnEditGroup, btnAddGroup, btnLeaveGroup, btnDeleteGroup;

    private GroupAdapter adapter;
    private boolean loadData, loadGUI, isEditMode;

    @Inject
    GroupPresenter presenter;
    private GroupComponent component;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        presenter.create();
        init();
        loadData = true;
        if (loadGUI) {
            bindData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        bindViews(view);
        bindData();
        presenter.getGroups();
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
        presenter.destroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.group_add:
                onAdd();
                break;
            case R.id.group_edit:
                onEdit();
                break;
            case R.id.group_leave:
                onLeave();
                break;
            case R.id.group_delete:
                onDelete();
                break;
        }
    }

    private void bindViews(View view) {
        bottomMenu = (RelativeLayout) view.findViewById(R.id.group_bottom_menu);
        listGroup = (RecyclerView) view.findViewById(R.id.group_recycle_view);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        searchView = (SearchView) view.findViewById(R.id.group_search_view);
//        CommonMethod.UpdateSearchViewLayout(searchView);

        btnEditGroup = (Button) view.findViewById(R.id.group_edit);
        btnEditGroup.setOnClickListener(this);
        btnAddGroup = (Button) view.findViewById(R.id.group_add);
        btnAddGroup.setOnClickListener(this);
        btnLeaveGroup = (Button) view.findViewById(R.id.group_leave);
        btnLeaveGroup.setOnClickListener(this);
        btnDeleteGroup = (Button) view.findViewById(R.id.group_delete);
        btnDeleteGroup.setOnClickListener(this);
    }

    private void init() {
        adapter = new GroupAdapter(this);
    }

    private void bindData() {
        listGroup.setAdapter(adapter);
        listGroup.setLayoutManager(linearLayoutManager);

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

    private void onEdit() {
        isEditMode = !isEditMode;
        updateEditMode();
    }

    private void onAdd() {
        if (!ServiceManager.getInstance().getNetworkStatus(getContext())) {
            Toast.makeText(getContext(), "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getActivity(), AddGroupActivity.class);
        startActivity(intent);
    }

    private void onLeave() {
        // FIXME: currently, this feature is disabled.
        /*showLoading();
        List<Group> selectedGroups = adapter.getSelectGroup();
        AtomicInteger counter = new AtomicInteger(0);
        Callback callback = (error, data) -> {
            counter.incrementAndGet();
            if (counter.get() == selectedGroups.size()) {
                hideLoading();
                adapter.cleanSelectGroup();
                switchOffEditMode();
            }
        };
        for (Group group : selectedGroups) {
            groupRepository.leaveGroup(group, callback);
        }*/
    }

    private void onDelete() {
        // FIXME: currently, this feature is disabled.
        /*List<Group> selectedGroups = adapter.getSelectGroup();
        ServiceManager.getInstance().deleteGroup(selectedGroups);
        adapter.cleanSelectGroup();
        switchOffEditMode();*/
    }

    private void updateEditMode() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.onEditMode(isEditMode);
        adapter.setEditMode(isEditMode);
        if (isEditMode) {
            btnEditGroup.setText("CANCEL");
            btnAddGroup.setVisibility(View.GONE);
            bottomMenu.setVisibility(View.VISIBLE);
        } else {
            btnEditGroup.setText("EDIT");
            btnAddGroup.setVisibility(View.VISIBLE);
            bottomMenu.setVisibility(View.GONE);
        }

        if (CollectionUtils.isEmpty(adapter.getSelectGroup())) {
            btnLeaveGroup.setEnabled(false);
            btnDeleteGroup.setEnabled(false);
        } else {
            btnLeaveGroup.setEnabled(true);
            btnDeleteGroup.setEnabled(true);
        }
    }

    @Override
    public void onSendMessage(Group group) {
        if (TextUtils.isEmpty(group.conversationID)) {
            presenter.createConversation(group);
        } else {
            moveToChatScreen(group.conversationID);
        }

    }

    @Override
    public void onViewProfile(Group group, Pair<View, String>... sharedElements) {
        Intent intent = new Intent(getContext(), ConversationDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString(ConversationDetailActivity.CONVERSATION_KEY, group.conversationID);
        intent.putExtra(ConversationDetailActivity.EXTRA_IMAGE_KEY, sharedElements[0].second);
        intent.putExtras(extras);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                sharedElements
        );
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onSelect(ArrayList<Group> groups) {
        updateEditMode();
    }

    public GroupComponent getComponent() {
        if (component == null) {
            component = getComponent(MainComponent.class)
                    .provideGroupComponent(new GroupModule(this));
        }
        return component;
    }

    private void scrollToTop() {
        listGroup.scrollToPosition(0);
    }

    @Override
    public void addGroup(Group group) {
        adapter.addGroup(group);
        scrollToTop();
    }

    @Override
    public void updateGroup(Group group) {
        adapter.updateGroup(group);
    }

    @Override
    public void deleteGroup(Group data) {
        adapter.deleteGroup(data.key);
    }

    @Override
    public void moveToChatScreen(String conversationId) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId);
        getContext().startActivity(intent);
    }
}
