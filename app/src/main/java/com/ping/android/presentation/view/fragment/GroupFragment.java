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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bzzzchat.configuration.GlideApp;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.ping.android.R;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.presentation.presenters.GroupPresenter;
import com.ping.android.presentation.view.activity.AddGroupActivity;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.presentation.view.activity.ConversationDetailActivity;
import com.ping.android.presentation.view.activity.MainActivity;
import com.ping.android.presentation.view.adapter.GroupAdapter;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class GroupFragment extends BaseFragment implements View.OnClickListener, GroupAdapter.GroupListener, GroupPresenter.View {
    private RelativeLayout bottomMenu;
    private RecyclerView listGroup;
    private LinearLayoutManager linearLayoutManager;
    private EditText searchView;
    private Button btnEditGroup, btnAddGroup, btnLeaveGroup, btnDeleteGroup;

    private GroupAdapter adapter;
    private boolean loadData, loadGUI, isEditMode;

    @Inject
    GroupPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
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
        bottomMenu = view.findViewById(R.id.group_bottom_menu);
        listGroup = view.findViewById(R.id.group_recycle_view);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        searchView = view.findViewById(R.id.group_search_view);
//        CommonMethod.UpdateSearchViewLayout(searchView);

        btnEditGroup = view.findViewById(R.id.group_edit);
        btnEditGroup.setOnClickListener(this);
        btnAddGroup = view.findViewById(R.id.group_add);
        btnAddGroup.setOnClickListener(this);
        btnLeaveGroup = view.findViewById(R.id.group_leave);
        btnLeaveGroup.setOnClickListener(this);
        btnDeleteGroup = view.findViewById(R.id.group_delete);
        btnDeleteGroup.setOnClickListener(this);
    }

    private void init() {
        adapter = new GroupAdapter(GlideApp.with(this), this);
    }

    private void bindData() {
        listGroup.setAdapter(adapter);
        listGroup.setLayoutManager(linearLayoutManager);

        registerEvent(RxTextView.textChanges(searchView)
                .subscribe(charSequence -> adapter.filter(charSequence.toString())));

        updateEditMode();
    }

    private void onEdit() {
        isEditMode = !isEditMode;
        updateEditMode();
    }

    private void onAdd() {
        if (!isNetworkAvailable()) {
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

        if (adapter.getSelectGroup().size() == 0) {
            btnLeaveGroup.setEnabled(false);
            btnDeleteGroup.setEnabled(false);
        } else {
            btnLeaveGroup.setEnabled(true);
            btnDeleteGroup.setEnabled(true);
        }
    }

    @Override
    public void onSendMessage(Group group) {
        presenter.handleGroupPress(group);
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
    public void moveToChatScreen(Conversation conversation) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversation.key);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_NAME, conversation.conversationName);
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, conversation.currentColor.getCode());
        //intent.putExtras(bundle);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getActivity(),
                android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        startActivity(intent, bundle);
    }
}
