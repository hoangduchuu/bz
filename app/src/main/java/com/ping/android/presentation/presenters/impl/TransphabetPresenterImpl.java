package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.user.UpdateUserTransphabetUseCase;
import com.ping.android.presentation.presenters.TransphabetPresenter;

import java.util.Map;

import javax.inject.Inject;

public class TransphabetPresenterImpl implements TransphabetPresenter {
    @Inject
    UpdateUserTransphabetUseCase updateUserTransphabetUseCase;

    @Inject
    public TransphabetPresenterImpl() {}

    @Override
    public void randomizeTransphabet(Map<String, String> mappings) {
        updateUserTransphabetUseCase.execute(new DefaultObserver<>(), mappings);
    }
}
