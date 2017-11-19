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
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.activity.AddGroupActivity;
import com.ping.android.activity.ChatActivity;
import com.ping.android.activity.GroupProfileActivity;
import com.ping.android.activity.MainActivity;
import com.ping.android.activity.R;
import com.ping.android.adapter.GroupAdapter;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment implements View.OnClickListener, GroupAdapter.ClickListener {

    private FirebaseAuth auth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    private RelativeLayout bottomMenu;
    private RecyclerView listGroup;
    private LinearLayoutManager linearLayoutManager;
    private SearchView searchView;
    private Button btnEditGroup, btnAddGroup, btnLeaveGroup, btnDeleteGroup;

    private User currentUser;
    private GroupAdapter adapter;
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
        View view = inflater.inflate(R.layout.fragment_group, container, false);
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
        CommonMethod.UpdateSearchViewLayout(searchView);

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
        auth = FirebaseAuth.getInstance();
        mFirebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        currentUser = ServiceManager.getInstance().getCurrentUser();
        adapter = new GroupAdapter(getContext(), this);
        getGroup();
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
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
        updateEditMode();
    }

    private void getGroup() {
        mDatabase.child("users").child(currentUser.key).child("groups").addChildEventListener(new ChildEventListener() {

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
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void insertOrUpdateMessage(DataSnapshot dataSnapshot, Boolean isAddNew) {
        Group group = new Group(dataSnapshot);
        group.key = dataSnapshot.getKey();
        if (MapUtils.isEmpty(group.memberIDs)) {
            return;
        }
        if(ServiceManager.getInstance().getCurrentDeleteStatus(group.deleteStatuses)) {
            if (!isAddNew) {
                adapter.deleteGroup(group.key);
            }
            return;
        }
        ServiceManager.getInstance().initMembers(new ArrayList<String>(group.memberIDs.keySet()), new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                group.members = (List<User>) data[0];
                adapter.addOrUpdateConversation(group);
            }
        });
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
        List<Group> selectedGroups = adapter.getSelectGroup();
        ServiceManager.getInstance().leaveGroup(selectedGroups);
        adapter.cleanSelectGroup();
        updateEditMode();
    }

    private void onDelete() {
        List<Group> selectedGroups = adapter.getSelectGroup();
        ServiceManager.getInstance().deleteGroup(selectedGroups);
        adapter.cleanSelectGroup();
        updateEditMode();
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
        if (StringUtils.isEmpty(group.conversationID)) {
            ServiceManager.getInstance().createConversationForGroupChat(currentUser.key, group, new Callback() {
                @Override
                public void complete(Object error, Object... data) {
                    group.conversationID = data[0].toString();
                    sendMessage(group);
                }
            });
        } else {
            sendMessage(group);
        }

    }

    @Override
    public void onViewProfile(Group group) {
        Intent intent = new Intent(getContext(), GroupProfileActivity.class);
        intent.putExtra(Constant.START_ACTIVITY_GROUP_ID, group.key);
        getContext().startActivity(intent);
    }

    @Override
    public void onSelect(ArrayList<Group> groups){
        updateEditMode();
    }

    private void sendMessage(Group group) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("CONVERSATION_ID", group.conversationID);
        getContext().startActivity(intent);
    }
}
