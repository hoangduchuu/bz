package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.user.UpdatePhoneNumberUseCase;
import com.ping.android.presentation.presenters.AddPhonePresenter;

import javax.inject.Inject;

public class AddPhonePresenterImpl implements AddPhonePresenter {
    @Inject
    View view;
    @Inject
    UpdatePhoneNumberUseCase updatePhoneNumberUseCase;

    @Inject
    public AddPhonePresenterImpl() {}

    @Override
    public void updatePhone(String phoneNumber) {
        updatePhoneNumberUseCase.execute(new DefaultObserver<>(), phoneNumber);
    }

    @Override
    public void destroy() {
        updatePhoneNumberUseCase.dispose();
    }
}
