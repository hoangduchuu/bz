package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.Color;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.VoiceType;
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.MessageHeaderItem;

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

    void sendAudioMessage(String audioUrl, VoiceType voiceType);

    void sendVideoMessage(String videoPath);

    void resendMessage(Message message);

    void updateConversationLastMessage(Message lastMessage);

    void deleteMessages(List<Message> messages);

    void updateMaskMessages(List<Message> messages, boolean isLastMessage, boolean isMask);

    void updateConversationReadStatus();

    void handleUserTypingStatus(boolean typing);

    void handleVideoCallPress();

    void handleVoiceCallPress();

    void initThemeColor(Color currentColor);

    interface View extends BaseView {
        void updateConversation(Conversation conversation);

        void updateConversationTitle(String title);

        void updateMaskSetting(boolean isEnable);

        void updateUserStatus(boolean isOnline);

        void hideUserStatus();

        void removeMessage(MessageHeaderItem headerItem, MessageBaseItem data);

        void updateLastMessages(List<MessageHeaderItem> messages, boolean canLoadMore);

        void switchOffEditMode();

        void updateNickNames(Map<String, String> nickNames);

        void toggleTyping(boolean b);

        void showErrorUserBlocked(String username);

        void openCallScreen(User currentUser, User opponentUser, boolean isVideoCall);

        void updateMessage(MessageBaseItem item, MessageHeaderItem headerItem, boolean added);

        void changeTheme(Color from);

        void updateBackground(String s);

        void hideRefreshView();
    }
}
