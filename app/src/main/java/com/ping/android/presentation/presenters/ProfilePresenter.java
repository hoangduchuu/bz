package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.User;

/**
 * Created by tuanluong on 2/8/18.
 */

public interface ProfilePresenter extends BasePresenter {
    void logout();

    void toggleNotificationSetting(boolean checked);

    void togglePrivateProfileSetting(boolean checked);

    void uploadUserProfile(String profileFilePath);

    void checkPasswordBeforeDeleteFaceData(String password);

    void checkPasswordBeforeTurnOffFaceData(String trim);

    void onTrainingFaceTextClicked();

    void onRequestTurnOffFaceData();

    interface View extends BaseView {

        void updateUser(User user);

        void navigateToLogin();

        void showErrorLogoutFailed();

        void showRequirePasswordFormBeforeDeleteFaceData();

        void handleDeleteFaceIdSuccess();
        void handleConfirmPasswordError(String errorMsg);

        void handleRequireTurnOffFaceIDSError(String errorMsg);
        void handleRequireTurnOffFaceIDSuccess();

        void showRequirePasswordFormBeforeTurnOffFaceData();

        void updateToggleIcon();

        void hideSetupText();
    }
}
