package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.google.firebase.FirebaseNetworkException;
import com.ping.android.domain.usecase.user.UpdatePhoneNumberUseCase;
import com.ping.android.presentation.presenters.AddPhonePresenter;

import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import io.reactivex.observers.DisposableObserver;

public class AddPhonePresenterImpl implements AddPhonePresenter {
    @Inject
    View view;
    @Inject
    UpdatePhoneNumberUseCase updatePhoneNumberUseCase;

    @Inject
    public AddPhonePresenterImpl() {
    }

    @Override
    public void updatePhone(String phoneNumber) {
        view.showLoading();
        updatePhoneNumberUseCase.execute(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.showLoading();

            }

            @Override
            public void onError(Throwable exception) {
                if (exception instanceof TimeoutException || exception instanceof FirebaseNetworkException) {
                    view.showTimeOutNotification();
                    view.hideLoading();
                } else {
                    view.hideLoading();
                }
            }

            @Override
            public void onComplete() {
                view.hideLoading();
            }
        }, phoneNumber);
    }

    @Override
    public void destroy() {
        updatePhoneNumberUseCase.dispose();
    }
}
