package com.ping.android.data.repository;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.ultility.Constant;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/26/18.
 */

public class MessageRepositoryImpl implements MessageRepository {
    FirebaseDatabase database;

    @Inject
    public MessageRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public Observable<DataSnapshot> getLastMessages(String conversationId) {
        DatabaseReference reference = database.getReference("messages").child(conversationId);
        reference.keepSynced(true);
        Query query = reference
                .orderByChild("timestamp")
                .limitToLast(Constant.LATEST_RECENT_MESSAGES);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .toObservable();
    }

    @Override
    public Observable<DataSnapshot> loadMoreMessages(String conversationId, double endTimestamp) {
        Query query = database.getReference("messages").child(conversationId)
                .orderByChild("timestamp")
                .endAt(endTimestamp)
                .limitToLast(Constant.LOAD_MORE_MESSAGE_AMOUNT + 1);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .toObservable();
    }

    @Override
    public Observable<DataSnapshot> loadConversationMedia(String conversationId, double lastTimestamp) {
        DatabaseReference reference = database.getReference("media").child(conversationId);
        reference.keepSynced(true);
        Query query = reference
                .orderByChild("timestamp")
                .endAt(lastTimestamp)
                .limitToLast(20);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .toObservable();
    }

    @Override
    public Observable<ChildEvent> observeMessageUpdate(String conversationId) {
        Query query = database.getReference("messages").child(conversationId);
        return RxFirebaseDatabase.getInstance(query)
                .onChildEvent();
    }

    @Override
    public Observable<ChildEvent> observeLastMessage(String conversationId) {
        Query query = database.getReference("messages").child(conversationId)
                .orderByChild("timestamp")
                .limitToLast(1);
        return RxFirebaseDatabase.getInstance(query)
                .onChildEvent();
    }

    @Override
    public Observable<Boolean> updateMessageStatus(String conversationId, String messageId, String userId, int status) {
        DatabaseReference reference = database.getReference("messages").child(conversationId).child(messageId).child("status").child(userId);
        return RxFirebaseDatabase.setValue(reference, status)
                .map(reference1 -> true)
                .toObservable();
    }
}
