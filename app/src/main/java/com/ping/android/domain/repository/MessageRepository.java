package com.ping.android.domain.repository;

import com.ping.android.data.entity.ChildData;
import com.ping.android.data.entity.MessageEntity;
import com.ping.android.model.Message;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;

/**
 * Created by tuanluong on 2/26/18.
 */

public interface MessageRepository {
    Observable<List<MessageEntity>> getLastMessages(String conversationId);

    Observable<List<MessageEntity>> loadMoreMessages(String conversationId, double endTimestamp);

    Observable<List<MessageEntity>> loadConversationMedia(String conversationId, double lastTimestamp);

    Observable<ChildData<MessageEntity>> observeMessageUpdate(String conversationId);

    Observable<ChildData<MessageEntity>> observeLastMessage(String conversationId);

    Observable<Boolean> updateMessageStatus(String conversationId, String messageId, String userId, int status);

    Observable<ChildData<MessageEntity>> observeMediaUpdate(String conversationId);

    Observable<String> updateThumbnailImage(String conversationKey, String messageKey, String filePath);

    Observable<String> updateImage(String conversationKey, String messageKey, String s);

    Observable<String> updateAudioUrl(String conversationKey, String messageKey, String s);

    Observable<String> updateVideoUrl(String conversationKey, String messageKey, String filePath);

    Observable<MessageEntity> addChildMessage(String conversationKey, String messageKey, MessageEntity data);

    Observable<MessageEntity> sendMediaMessage(String conversationId, MessageEntity message);

    String populateChildMessageKey(String conversationId, String messageId);

    Observable<Boolean> updateChildMessageImage(String conversationId, String parentMessageKey, String messageKey, String thumbnail, String image);

    Observable<Boolean> updateChildMessageGame(String conversationId, String parentMessageKey, String messageKey, String gameUrl);

    Observable<List<MessageEntity>> getCachedMessages(String conversationId);

    void deleteCacheMessage(String messageKey);

    void deleteCacheMessages(String conversationId);

    void saveMessage(MessageEntity entity);

    void saveMessages(List<MessageEntity> entities);

    void updateLocalMaskStatus(String message, boolean isMask);

    Observable<List<MessageEntity>> getErrorMessages();

    Observable<List<MessageEntity>> getUpdatedMessages(@NotNull String conversationId, double timestamp);

    void updateLocalMessageStatus(@NotNull String key, int status);

    Observable<Integer> getMessageStatus(String conversationId, String message, String userId);


    //region Huu additional

    /**
     *
     * @param conversationKey   : is conversationKey
     * @param messageKey        : is messageKey
     * @param currentUserKey    : currentUserKey
     * @return                  : input path
     */
    Observable<String> updateMsgStatus(String conversationKey, String messageKey, String currentUserKey,String filePath);
    // endregion
}
