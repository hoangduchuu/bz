package com.ping.android.domain.repository;

import com.google.firebase.database.DataSnapshot;
import com.ping.android.data.entity.MessageEntity;
import com.ping.android.model.Conversation;

import java.util.Map;

import durdinapps.rxfirebase2.RxFirebaseChildEvent;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface ConversationRepository {
    Observable<String> getKey();

    Observable<DataSnapshot> loadMoreConversation(String userId, double endTimestamps);

    Observable<Boolean> createConversation(Conversation conversation);

    Observable<Boolean> updateConversation(String userId, String conversationId, Map<String, Object> values);

    Observable<RxFirebaseChildEvent<DataSnapshot>> registerConversationsUpdate(String userId);

    Observable<DataSnapshot> observeConversationValue(String userId, String conversationId);

    Observable<String> getMessageKey(String conversationId);

    Observable<MessageEntity> sendMessage(Conversation conversation, MessageEntity message);

    Observable<Conversation> getConversation(String userKey, String conversationID);

    Observable<Map<String,Boolean>> observeTypingEvent(String conversationId, String userId);

    Observable<Boolean> updateReadStatus(String conversationId, String userId);

    Observable<String> getConversationNickName(String userId, String conversationID, String opponentUserId);

    Observable<Integer> observeConversationColor(String userId, String conversationId);

    Observable<String> observeConversationBackground(String userId, String conversationId);

    Observable<DataSnapshot> getDefaultBackgrounds();

    Single<Boolean> updateMaskOutput(String userId, String conversationId, Map<String, Boolean> memberIds, boolean mask);

    Observable<Map<String,String>> observeNicknames(String userId, String conversationId);

    Observable<Double> getDeleteMessageTimeStamp(String userId, String conversationId);
}
