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
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.presentation.view.activity.MainActivity;
import com.ping.android.activity.R;
import com.ping.android.presentation.view.activity.UserDetailActivity;
import com.ping.android.presentation.view.adapter.CallAdapter;
import com.ping.android.dagger.loggedin.main.MainComponent;
import com.ping.android.dagger.loggedin.main.call.CallComponent;
import com.ping.android.dagger.loggedin.main.call.CallModule;
import com.ping.android.model.Call;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.CallListPresenter;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.bus.BusProvider;
import com.ping.android.utils.bus.events.ConversationChangeEvent;
import com.ping.android.presentation.view.custom.CustomSwitch;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

public class CallFragment extends BaseFragment implements View.OnClickListener, CallAdapter.ClickListener, CallListPresenter.View {

    private final String TAG = "Ping: " + this.getClass().getSimpleName();

    private EditText searchView;
    private CustomSwitch customSwitch;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView rvListCall;
    private Button btnEditCall, btnDeleteCall;

    private CallAdapter adapter;
    private boolean isEditMode;
    private boolean isAll = true;
    private String search = "";
    private Button btnCancel;

    @Inject
    public BusProvider busProvider;
    @Inject
    public CallListPresenter presenter;
    private CallComponent component;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        listenConversationChange();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call, container, false);
        bindViews(view);
        bindData();
        presenter.create();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.pause();
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
        presenter.handleCallPressed(call, isVideoCall);
    }

    @Override
    public void onDeleteCall(Call call) {
        //ServiceManager.getInstance().deleteCall(call);
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
        rvListCall = view.findViewById(R.id.call_recycle_view);
        // FIXME: Enable load more feature but affect to search function
        /*rvListCall.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int totalItemCount = mLinearLayoutManager.getItemCount();
                int lastVisibleItem = mLinearLayoutManager
                        .findLastVisibleItemPosition();
                if (totalItemCount <= (lastVisibleItem + 5)) {
                    presenter.loadMore();
                }
            }
        });*/
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        searchView = view.findViewById(R.id.call_search_view);
        //CommonMethod.UpdateSearchViewLayout(searchView);
        customSwitch = view.findViewById(R.id.call_switch);

        btnEditCall = view.findViewById(R.id.call_edit);
        btnEditCall.setOnClickListener(this);

        btnDeleteCall = view.findViewById(R.id.call_delete);
        btnDeleteCall.setOnClickListener(this);

        btnCancel = view.findViewById(R.id.call_cancel_edit);
        btnCancel.setOnClickListener(this);
    }

    private void bindData() {
        adapter = new CallAdapter(new ArrayList<>(), this);
        rvListCall.setAdapter(adapter);
        rvListCall.setLayoutManager(mLinearLayoutManager);

        registerEvent(RxTextView.textChanges(searchView)
        .subscribe(charSequence -> {
            search = charSequence.toString();
            adapter.filter(charSequence.toString(), isAll);
        }));
        customSwitch.setSwitchToggleListener(switchToggleState -> {
            isAll = switchToggleState == CustomSwitch.SwitchToggleState.LEFT;
            adapter.filter(search, isAll);
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
        if (adapter.getSelectCall().size() == 0) {
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
        presenter.deleteCalls(selectedCalls);
//        for (Call call : selectedCalls) {
//            adapter.deleteCall(call.key);
//        }
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
        adapter.addOrUpdateCall(call);
        scrollToTop();
        if (getActivity() != null) {
            ((MainActivity) getActivity()).callAdded(call);
        }
    }

    @Override
    public void deleteCall(Call call) {
        adapter.deleteCall(call.key);
    }

    @Override
    public void callUser(User currentUser, User user, boolean isVideoCall) {
        CallActivity.start(getContext(), currentUser, user, isVideoCall);
    }

    @Override
    public void updateCalls(List<Call> callList) {
        adapter.updateData(callList);
    }

    @Override
    public void appendCalls(List<Call> callList) {
        adapter.appendCalls(callList);
    }

    private void listenConversationChange() {
        registerEvent(busProvider.getEvents()
                .subscribe(object -> {
                    if (object instanceof ConversationChangeEvent) {
                        adapter.updateNickNames(((ConversationChangeEvent) object).conversationId,
                                ((ConversationChangeEvent) object).nickName);
                    }
                }));
    }
}
