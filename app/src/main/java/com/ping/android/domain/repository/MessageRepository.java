package com.ping.android.domain.repository;

import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.model.Message;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/26/18.
 */

public interface MessageRepository {
    Observable<DataSnapshot> getLastMessages(String conversationId);

    Observable<DataSnapshot> loadMoreMessages(String conversationId, double endTimestamp);

    Observable<DataSnapshot> loadConversationMedia(String conversationId, double lastTimestamp);

    Observable<ChildEvent> observeMessageUpdate(String conversationId);

    Observable<ChildEvent> observeLastMessage(String conversationId);

    Observable<Boolean> updateMessageStatus(String conversationId, String messageId, String userId, int status);

    Observable<ChildEvent> observeMediaUpdate(String conversationId);
}
