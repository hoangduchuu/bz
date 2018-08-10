package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.DeleteCallsUseCase;
import com.ping.android.domain.usecase.GetCallsUseCase;
import com.ping.android.domain.usecase.ObserveCallUseCase;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.call.LoadMoreCallUseCase;
import com.ping.android.model.Call;
import com.ping.android.data.entity.ChildData;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.CallListPresenter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/30/18.
 */

public class CallListPresenterImpl implements CallListPresenter {
    @Inject
    LoadMoreCallUseCase loadMoreCallUseCase;
    @Inject
    ObserveCallUseCase observeCallUseCase;
    @Inject
    GetCallsUseCase getCallsUseCase;
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    @Inject
    DeleteCallsUseCase deleteCallsUseCase;
    @Inject
    CallListPresenter.View view;
    private User currentUser;
    private boolean canLoadMore = true;
    private double lastTimestamp = Double.MAX_VALUE;
    private AtomicBoolean isLoading;
    private TreeMap<Double, Call> callTreeMap = new TreeMap<>();

    @Inject
    public CallListPresenterImpl() {
        isLoading = new AtomicBoolean(false);
    }

    @Override
    public void create() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
            }
        }, null);
        getCalls();
    }

    @Override
    public void resume() {
        //getCalls();
    }

    @Override
    public void pause() {
        //observeCallUseCase.unsubscribe();
    }

    @Override
    public void getCalls() {
        loadMoreCallUseCase.execute(new DefaultObserver<LoadMoreCallUseCase.Output>() {
            @Override
            public void onNext(LoadMoreCallUseCase.Output output) {
                lastTimestamp = output.lastTimestamp;
                canLoadMore = output.canLoadMore;
                for (Call call : output.callList) {
                    callTreeMap.put(call.timestamp, call);
                }
                view.updateCalls(new ArrayList<>(callTreeMap.descendingMap().values()));
                observeCalls();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                observeCalls();
            }
        }, lastTimestamp);
    }

    private void observeCalls() {
        observeCallUseCase.execute(new DefaultObserver<ChildData<Call>>() {
            @Override
            public void onNext(ChildData<Call> callChildData) {
                switch (callChildData.getType()) {
                    case CHILD_ADDED:
                        Call call = callChildData.getData();
                        if (callTreeMap.get(call.timestamp) == null) {
                            callTreeMap.put(call.timestamp, call);
                            view.addCall(call);
                        }
                        break;
                    case CHILD_REMOVED:
                        view.deleteCall(callChildData.getData());
                        break;
                }
            }
        }, null);
    }

    @Override
    public void handleCallPressed(Call call, boolean isVideo) {
        User opponentUser = call.opponentUser;
        view.callUser(currentUser, opponentUser, isVideo);
    }

    @Override
    public void deleteCalls(ArrayList<Call> selectedCalls) {
        deleteCallsUseCase.execute(new DefaultObserver<>(), selectedCalls);
    }

    @Override
    public void loadMore() {
        if (isLoading.get() || !canLoadMore) return;
        isLoading.set(true);
        lastTimestamp = lastTimestamp - 0.001;
        loadMoreCallUseCase.execute(new DefaultObserver<LoadMoreCallUseCase.Output>() {
            @Override
            public void onNext(LoadMoreCallUseCase.Output output) {
                canLoadMore = output.canLoadMore;
                lastTimestamp = output.lastTimestamp;
                view.appendCalls(output.callList);
                isLoading.set(false);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, lastTimestamp);
    }

    @Override
    public void destroy() {
        view = null;
        observeCallUseCase.dispose();
        observeCurrentUserUseCase.dispose();
        deleteCallsUseCase.dispose();
    }
}
