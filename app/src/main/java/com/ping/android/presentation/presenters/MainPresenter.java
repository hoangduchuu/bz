package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.enums.Color;

import java.util.Map;

/**
 * Created by tuanluong on 2/10/18.
 */

public interface MainPresenter extends BasePresenter {
    void removeMissedCallsBadge();

    void onNetworkAvailable();

    void turnOffMappingConfirmation();

    void randomizeTransphabet(Map<String, String> maps);

    void handleNewConversation(String conversationId);

    interface View extends BaseView {

        void openPhoneRequireView();

        void showMappingConfirm();

        void startCallService();

        void moveToChatScreen(String conversationId, Color color);
    }
}
