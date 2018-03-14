package com.ping.android.presentation.view.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ping.android.activity.CallActivity;
import com.ping.android.presentation.view.activity.MainActivity;
import com.ping.android.activity.R;
import com.ping.android.presentation.view.activity.UserDetailActivity;
import com.ping.android.presentation.view.adapter.CallAdapter;
import com.ping.android.dagger.loggedin.main.MainComponent;
import com.ping.android.dagger.loggedin.main.call.CallComponent;
import com.ping.android.dagger.loggedin.main.call.CallModule;
import com.ping.android.fragment.BaseFragment;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Constant;
import com.ping.android.view.CustomSwitch;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;

import javax.inject.Inject;

public class CallFragment extends BaseFragment implements View.OnClickListener, CallAdapter.ClickListener, CallPresenter.View {

    private final String TAG = "Ping: " + this.getClass().getSimpleName();

    private SearchView searchView;
    private CustomSwitch customSwitch;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView rvListCall;
    private Button btnEditCall, btnDeleteCall;

    private User currentUser;
    private CallAdapter adapter;
    private boolean isEditMode;
    private boolean isAll = true;
    private String search = "";
    private Button btnCancel;

    private UserRepository userRepository;
    private SharedPreferences prefs;

    @Inject
    public CallPresenter presenter;
    private CallComponent component;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call, container, false);
        bindViews(view);
        bindData();
        presenter.getCalls();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.destroy();
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

    @Override
    public void onViewProfile(User user, Pair<View, String>... sharedElements) {
        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
        intent.putExtra(Constant.START_ACTIVITY_USER_ID, user.key);
        intent.putExtra(UserDetailActivity.EXTRA_USER, user);
        intent.putExtra(UserDetailActivity.EXTRA_USER_IMAGE, sharedElements[0].second);
        intent.putExtra(UserDetailActivity.EXTRA_USER_NAME, sharedElements[1].second);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                sharedElements
        );
        startActivity(intent, options.toBundle());
    }

    private void bindViews(View view) {
        rvListCall = (RecyclerView) view.findViewById(R.id.call_recycle_view);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        searchView = (SearchView) view.findViewById(R.id.call_search_view);
        //CommonMethod.UpdateSearchViewLayout(searchView);
        customSwitch = (CustomSwitch) view.findViewById(R.id.call_switch);

        btnEditCall = (Button) view.findViewById(R.id.call_edit);
        btnEditCall.setOnClickListener(this);

        btnDeleteCall = (Button) view.findViewById(R.id.call_delete);
        btnDeleteCall.setOnClickListener(this);

        btnCancel = (Button) view.findViewById(R.id.call_cancel_edit);
        btnCancel.setOnClickListener(this);
    }

    private void init() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        userRepository = new UserRepository();
        currentUser = UserManager.getInstance().getUser();
    }

    private void bindData() {
        adapter = new CallAdapter(new ArrayList<>(), this);
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

    private void scrollToTop() {
        rvListCall.scrollToPosition(0);
    }

    public CallComponent getComponent() {
        if (component == null) {
            component = getComponent(MainComponent.class).provideCallComponent(new CallModule(this));
        }
        return component;
    }

    @Override
    public void addCall(Call call) {
        adapter.addCall(call);
        scrollToTop();
        if (getActivity() != null) {
            ((MainActivity) getActivity()).callAdded(call);
        }
    }

    @Override
    public void deleteCall(Call call) {
        adapter.deleteCall(call.key);
    }
}
