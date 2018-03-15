package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveCallUseCase;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.model.Call;
import com.ping.android.model.ChildData;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.CallPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/30/18.
 */

public class CallPresenterImpl implements CallPresenter {
    @Inject
    ObserveCallUseCase observeCallUseCase;
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    @Inject
    CallPresenter.View view;
    private User currentUser;

    @Inject
    public CallPresenterImpl() {}

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
        observeCallUseCase.execute(new DefaultObserver<ChildData<Call>>() {
            @Override
            public void onNext(ChildData<Call> callChildData) {
                switch (callChildData.type) {
                    case CHILD_ADDED:
                        view.addCall(callChildData.data);
                        break;
                    case CHILD_REMOVED:
                        view.deleteCall(callChildData.data);
                        break;
                }
            }
        }, null);
    }

    @Override
    public void handleCallPressed(Call call, boolean isVideo) {
        for (User user : call.members) {
            if (!user.key.equals(this.currentUser.key)) {
                view.callUser(user, isVideo);
                break;
            }
        }
    }

    @Override
    public void destroy() {
        observeCallUseCase.dispose();
        observeCurrentUserUseCase.dispose();
    }
}
