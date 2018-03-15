package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.GameType;
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem;

import java.util.List;
import java.util.Map;

/**
 * Created by tuanluong on 2/26/18.
 */

public interface ChatPresenter extends BasePresenter {
    void initConversationData(String conversationId);

    void observeMessageUpdate();

    void loadMoreMessage(double oldestTimestamp);

    void sendTextMessage(String message, boolean markStatus);

    void sendImageMessage(String photoUrl, String thumbUrl, boolean markStatus);

    void sendGameMessage(String gameUrl, GameType gameType, boolean markStatus);

    void sendAudioMessage(String audioUrl);

    void resendMessage(Message message);

    void updateConversationLastMessage(Message lastMessage);

    void deleteMessages(List<Message> messages);

    void updateMaskMessages(List<Message> messages, boolean isLastMessage, boolean isMask);

    void updateConversationReadStatus();

    void handleUserTypingStatus(boolean typing);

    interface View extends BaseView {
        void updateConversation(Conversation conversation);

        void updateConversationTitle(String title);

        void updateMaskSetting(boolean isEnable);

        void updateUserStatus(boolean isOnline);

        void hideUserStatus();

        void addNewMessage(MessageBaseItem data);

        void removeMessage(Message data);

        void updateMessage(MessageBaseItem data);

        void updateLastMessages(List<MessageBaseItem> messages, boolean canLoadMore);

        void sendNotification(Conversation conversation, Message message);

        void addCacheMessage(MessageBaseItem message);

        void switchOffEditMode();

        void updateNickNames(Map<String, String> nickNames);

        void toggleTyping(boolean b);

        void showErrorUserBlocked(String username);
    }
}
