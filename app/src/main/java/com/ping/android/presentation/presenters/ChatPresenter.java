package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.bzzzchat.videorecorder.view.PhotoItem;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.Color;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.VoiceType;
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.MessageHeaderItem;

import org.jetbrains.annotations.NotNull;

import java.io.File;
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

    void sendImagesMessage(List<PhotoItem> items, boolean markStatus);

    void sendGameMessage(String gameUrl, GameType gameType, boolean markStatus);

    void sendGameMessages(@NotNull List<PhotoItem> items, GameType gameType, boolean isMask);

    void sendAudioMessage(String audioUrl, VoiceType voiceType);

    void sendVideoMessage(String videoPath);

    void resendMessage(Message message);

    void updateConversationLastMessage(Message lastMessage);

    void deleteMessages(List<Message> messages);

    void updateMaskMessages(List<Message> messages, boolean isLastMessage, boolean isMask, boolean updateChild);

    void updateConversationReadStatus();

    void handleUserTypingStatus(boolean typing);

    void handleVideoCallPress();

    void handleVoiceCallPress();

    void initThemeColor(Color currentColor);

    void updateMaskOutput(boolean checked);

    void updateMaskChildMessages(List<Message> messages, boolean maskStatus);

    void getUpdatedMessages(double timestamp);

    void sendSticker(@NotNull File file, boolean isMask);

    void userRecognized();
    void sendSticker(String stickerPath);

    void sendGifs(String gifUrl);

    void checkPassword(@NotNull String password);

    interface View extends BaseView {
        void updateConversation(Conversation conversation);

        void updateConversationTitle(String title);

        void updateMaskSetting(boolean isEnable);

        void updateUserStatus(boolean isOnline);

        void hideUserStatus();

        void removeMessage(MessageHeaderItem headerItem, MessageBaseItem data);

        void updateLastMessages(List<MessageHeaderItem> messages, boolean canLoadMore);

        void appendHistoryMessages(List<MessageHeaderItem> messages, boolean canLoadMore);

        void updateNickNames(Map<String, String> nickNames);

        void toggleTyping(boolean b);

        void showErrorUserBlocked(String username);

        void openCallScreen(User currentUser, User opponentUser, boolean isVideoCall);

        void updateMessage(MessageBaseItem item, MessageHeaderItem headerItem, MessageHeaderItem higherHeaderItem, boolean added);

        void changeTheme(Color from);

        void updateBackground(String s);

        void hideRefreshView();

        void refreshMessages();

        void updateUnreadMessageCount(int count);

        void showFaceDetectFailedAfter10s();

        void showRequirePasswordForm();

        void disableFaceID();

        void displayConfirmPasswordError(String message);

    }
}
