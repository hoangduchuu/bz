package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.LogoutUseCase;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.user.ToggleUserNotificationSettingUseCase;
import com.ping.android.domain.usecase.user.ToggleUserPrivateProfileSettingUseCase;
import com.ping.android.domain.usecase.user.UploadUserProfileImageUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ProfilePresenter;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/8/18.
 */

public class ProfilePresenterImpl implements ProfilePresenter {
    @Inject
    ProfilePresenter.View view;
    @Inject
    LogoutUseCase logoutUseCase;
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    @Inject
    ToggleUserNotificationSettingUseCase toggleUserNotificationSettingUseCase;
    @Inject
    ToggleUserPrivateProfileSettingUseCase toggleUserPrivateProfileSettingUseCase;
    @Inject
    UploadUserProfileImageUseCase uploadUserProfileImageUseCase;

    @Inject
    public ProfilePresenterImpl() {}

    @Override
    public void create() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                view.updateUser(user);
            }
        }, null);
    }

    @Override
    public void logout() {
        view.showLoading();
        logoutUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                super.onNext(aBoolean);
                view.hideLoading();
                view.navigateToLogin();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
                view.showErrorLogoutFailed();
                exception.printStackTrace();
            }
        }, null);
    }

    @Override
    public void toggleNotificationSetting(boolean checked) {
        view.showLoading();
        toggleUserNotificationSettingUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, checked);
    }

    @Override
    public void togglePrivateProfileSetting(boolean checked) {
        view.showLoading();
        toggleUserPrivateProfileSettingUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, checked);
    }

    @Override
    public void uploadUserProfile(String profileFilePath) {
        view.showLoading();
        uploadUserProfileImageUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, profileFilePath);
    }

    @Override
    public void destroy() {
        logoutUseCase.dispose();
        observeCurrentUserUseCase.dispose();
        toggleUserNotificationSettingUseCase.dispose();
        toggleUserPrivateProfileSettingUseCase.dispose();
        uploadUserProfileImageUseCase.dispose();
    }
}
