package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;

/**
 * Created by tuanluong on 1/31/18.
 */

public interface ConversationPVPDetailPresenter extends BasePresenter {
    void initConversation(String conversationId);

    void toggleNotification(boolean isEnable);

    void toggleMask(boolean isEnable);

    void togglePuzzle(boolean isEnable);

    void handleNicknameClicked();

    interface View extends BaseView {

        void updateConversation(Conversation conversation);

        void updateNotification(boolean isEnable);

        void updateMask(boolean isEnable);

        void openNicknameScreen(Conversation conversation);

        void updatePuzzlePicture(boolean isEnable);

        void updateBlockStatus(User user);
    }
}
