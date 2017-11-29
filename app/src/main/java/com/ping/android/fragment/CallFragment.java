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
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.activity.CallActivity;
import com.ping.android.activity.MainActivity;
import com.ping.android.activity.R;
import com.ping.android.adapter.CallAdapter;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.view.CustomSwitch;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class CallFragment extends Fragment implements View.OnClickListener, CallAdapter.ClickListener {

    private final String TAG = "Ping: " + this.getClass().getSimpleName();
    private FirebaseAuth auth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase, mCallDatabase;
    private ChildEventListener observeCallEvent;

    private SearchView searchView;
    private CustomSwitch customSwitch;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView rvListCall;
//    private RelativeLayout bottomMenu;
    private Button btnEditCall, btnDeleteCall;

    private User currentUser;
    private CallAdapter adapter;
    private ArrayList<Call> calls;
    private boolean loadData, loadGUI, isEditMode;
    private boolean isAll = true;
    private String search = "";
    private Button btnCancel;

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
        View view = inflater.inflate(R.layout.fragment_call, container, false);
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
    public void onDestroy() {
        super.onDestroy();
        if (mCallDatabase != null) {
            mCallDatabase.removeEventListener(observeCallEvent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.call_edit:
                onEdit();
                break;
            case R.id.call_cancel_edit:
                onExitEdit();
                break;
            case R.id.call_delete:
                onDelete();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onReCall(Call call, Boolean isVideoCall) {
        User user = null;
        for (User member : call.members) {
            if (!member.key.equals(currentUser.key)) {
                user = member;
                break;
            }
        }
        CallActivity.start(getContext(), user, isVideoCall);
    }

    @Override
    public void onDeleteCall(Call call) {
        ServiceManager.getInstance().deleteCall(call);
        // TODO exit edit when there is no record
    }

    @Override
    public void onSelect(ArrayList<Call> selectConversations) {
        updateEditMode();
    }

    private void bindViews(View view) {
        rvListCall = (RecyclerView) view.findViewById(R.id.call_recycle_view);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        searchView = (SearchView) view.findViewById(R.id.call_search_view);
        CommonMethod.UpdateSearchViewLayout(searchView);
        customSwitch = (CustomSwitch) view.findViewById(R.id.call_switch);

        btnEditCall = (Button) view.findViewById(R.id.call_edit);
        btnEditCall.setOnClickListener(this);

        btnDeleteCall = (Button) view.findViewById(R.id.call_delete);
        btnDeleteCall.setOnClickListener(this);

        btnCancel = (Button) view.findViewById(R.id.call_cancel_edit);
        btnCancel.setOnClickListener(this);
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        mFirebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        currentUser = ServiceManager.getInstance().getCurrentUser();
        calls = new ArrayList<>();
        adapter = new CallAdapter(calls, this);
        observeCalls();
    }

    private void bindData() {
        rvListCall.setAdapter(adapter);
        rvListCall.setLayoutManager(mLinearLayoutManager);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search = query;
                adapter.filter(query, isAll);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search = newText;
                adapter.filter(newText, isAll);
                return true;
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
        customSwitch.setSwitchToggleListener(new CustomSwitch.SwitchToggleListener() {
            @Override
            public void onSwitchToggle(CustomSwitch.SwitchToggleState switchToggleState) {
                isAll = switchToggleState == CustomSwitch.SwitchToggleState.LEFT ? true : false;
                adapter.filter(search, isAll);
            }
        });
        updateEditMode();
    }

    private void updateEditMode() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.onEditMode(isEditMode);
        adapter.setEditMode(isEditMode);

        if (isEditMode) {
            btnEditCall.setVisibility(View.GONE);
            btnCancel.setVisibility(View.VISIBLE);
            btnDeleteCall.setVisibility(View.VISIBLE);
        } else {
            btnEditCall.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.GONE);
            btnDeleteCall.setVisibility(View.GONE);
        }

        updateEditMenu();
    }

    private void updateEditMenu() {
        if (CollectionUtils.isEmpty(adapter.getSelectCall())) {
            btnDeleteCall.setEnabled(false);
        } else {
            btnDeleteCall.setEnabled(true);
        }
    }

    private void observeCalls() {
        observeCallEvent = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                insertOrUpdateCall(dataSnapshot, true);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                insertOrUpdateCall(dataSnapshot, false);
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
        mCallDatabase = mDatabase.child("users").child(mFirebaseUser.getUid()).child("calls");
        mCallDatabase.addChildEventListener(observeCallEvent);
    }

    private void insertOrUpdateCall(DataSnapshot dataSnapshot, Boolean isAddNew) {
        Call call = Call.from(dataSnapshot);
        if(ServiceManager.getInstance().getCurrentDeleteStatus(call.deleteStatuses)) {
            if (!isAddNew) {
                adapter.deleteCall(call.key);
            }
            return;
        }
        ArrayList<String> memberIDs = new ArrayList<>();
        memberIDs.add(call.senderId);
        memberIDs.add(call.receiveId);
        ServiceManager.getInstance().initMembers(memberIDs, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                call.members = (List<User>) data[0];
                for (User user : call.members) {
                    if (!user.key.equals(currentUser.key)) {
                        call.opponentUser = user;
                        break;
                    }
                }
                adapter.addOrUpdateCall(call);
            }
        });
    }

    private void onEdit() {
        isEditMode = true;
        updateEditMode();
    }

    private void onExitEdit() {
        isEditMode = false;
        updateEditMode();
        adapter.setEditMode(isEditMode);
    }

    private void onDelete() {
        ArrayList<Call> selectedCalls = new ArrayList<>(adapter.getSelectCall());
        ServiceManager.getInstance().deleteCalls(selectedCalls);
        for (Call call : selectedCalls) {
            adapter.deleteCall(call.key);
        }
        adapter.cleanSelectCall();
        onExitEdit();
    }
}
