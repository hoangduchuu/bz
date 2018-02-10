package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.presentation.presenters.MainPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/10/18.
 */

public class MainPresenterImpl implements MainPresenter {
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;

    @Inject
    public MainPresenterImpl() {

    }

    @Override
    public void create() {
        observeCurrentUserUseCase.execute(new DefaultObserver<>(), null);
    }

    @Override
    public void destroy() {
        observeCurrentUserUseCase.dispose();
    }
}
