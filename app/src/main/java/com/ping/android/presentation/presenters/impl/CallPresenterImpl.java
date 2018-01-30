package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveCallUseCase;
import com.ping.android.model.Call;
import com.ping.android.model.ChildData;
import com.ping.android.presentation.presenters.CallPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/30/18.
 */

public class CallPresenterImpl implements CallPresenter {
    @Inject
    ObserveCallUseCase observeCallUseCase;
    @Inject
    CallPresenter.View view;

    @Inject
    public CallPresenterImpl() {}

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
    public void destroy() {
        observeCallUseCase.dispose();
    }
}
