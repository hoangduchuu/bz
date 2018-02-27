package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;

import java.util.List;

/**
 * Created by tuanluong on 2/26/18.
 */

public interface ChatPresenter extends BasePresenter {
    void initConversationData(String conversationId);

    void observeMessageUpdate();

    void loadMoreMessage(double oldestTimestamp);

    void sendTextMessage();

    interface View extends BaseView {
        void updateConversation(Conversation conversation);

        void updateConversationTitle(String title);

        void updateMaskSetting(boolean isEnable);

        void updateUserStatus(boolean isOnline);

        void hideUserStatus();

        void onCurrentUser(User user);

        void addNewMessage(Message data);

        void removeMessage(Message data);

        void updateMessage(Message data);

        void updateLastMessages(List<Message> messages, boolean canLoadMore);
    }
}
