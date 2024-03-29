package com.ping.android.data.repository;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ping.android.data.entity.MessageEntity;
import com.ping.android.data.mappers.ConversationMapper;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.utils.configs.Constant;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ConversationRepositoryImpl implements ConversationRepository {
    public static final String CHILD_CONVERSATION = "conversations";
    FirebaseDatabase database;

    @Inject
    ConversationMapper mapper;

    @Inject
    public ConversationRepositoryImpl() {
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public Observable<String> getKey() {
        String key = database.getReference("conversations").push().getKey();
        return Observable.just(key);
    }

    @Override
    public Observable<DataSnapshot> loadMoreConversation(String userId, double endTimestamps) {
        DatabaseReference reference = database.getReference("conversations")
                .child(userId);
        reference.keepSynced(true);
        Query query = reference
                .orderByChild("timesstamps")
                .endAt(endTimestamps)
                .limitToLast(15);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .toObservable();
    }

    @Override
    public Observable<ChildEvent> registerConversationsUpdate(String userId) {
        DatabaseReference reference = database.getReference("conversations")
                .child(userId);
        reference.keepSynced(true);
        Query query = reference
                .orderByChild("timesstamps");
        //.limitToLast(15);
        return RxFirebaseDatabase.getInstance(query).onChildEvent();
    }

    @Override
    public Observable<DataSnapshot> observeConversationValue(String userId, String conversationId) {
        Query query = database.getReference("conversations").child(userId).child(conversationId);
        return RxFirebaseDatabase.getInstance(query).onValueEvent();
    }

    @Override
    public Observable<String> getMessageKey(String conversationId) {
        String key = database.getReference("messages").child(conversationId).push().getKey();
        return Observable.just(key);
    }

    @Override
    public Observable<MessageEntity> sendMessage(Conversation conversation, MessageEntity message) {
        Map<String, Object> updateValue = new HashMap<>();
        updateValue.put(String.format("messages/%s/%s", conversation.key, message.key), message.toMap());
        for (String toUser : conversation.memberIDs.keySet()) {
            if (!message.isReadable(toUser)) continue;
            updateValue.put(String.format("conversations/%s/%s", toUser, conversation.key), conversation.toMap());
            if (message.messageType == Constant.MSG_TYPE_IMAGE
                    || message.messageType == Constant.MSG_TYPE_GAME
                    || message.messageType == Constant.MSG_TYPE_IMAGE_GROUP) {
                updateValue.put(String.format("media/%s/%s", toUser, conversation.key), conversation.toMap());
            }
        }
        return RxFirebaseDatabase.updateBatchData(database.getReference(), updateValue)
                .map(aBoolean -> message)
                .toObservable();
    }

    @Override
    public Observable<Conversation> getConversation(String userKey, String conversationID) {
        Query query = database.getReference("conversations")
                .child(userKey).child(conversationID);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .map(dataSnapshot -> mapper.transform(dataSnapshot, userKey))
                .toObservable();
    }

    @Override
    public Observable<Map<String, Boolean>> observeTypingEvent(String conversationId, String userId) {
        Query query = database.getReference("conversations").child(userId).child(conversationId).child("typingIndicator");
        return RxFirebaseDatabase.getInstance(query)
                .onValueEvent()
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        return (Map<String, Boolean>) dataSnapshot.getValue();
                    } else {
                        return new HashMap<>();
                    }
                });
    }

    @Override
    public Observable<Boolean> updateReadStatus(String conversationId, String userId) {
        DatabaseReference reference = database.getReference("conversations")
                .child(userId).child(conversationId).child("readStatuses").child(userId);
        return RxFirebaseDatabase.setValue(reference, true)
                .map(reference1 -> true)
                .toObservable();
    }

    @Override
    public Observable<String> getConversationNickName(String userId, String conversationID, String opponentUserId) {
        Query query = database.getReference("conversations").child(userId).child(conversationID).child("nickNames").child(opponentUserId);
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        return dataSnapshot.getValue(String.class);
                    } else {
                        return "";
                    }
                })
                .toObservable();
    }

    @Override
    public Observable<Integer> observeConversationColor(String userId, String conversationId) {
        Query query = database.getReference("conversations").child(userId).child(conversationId)
                .child("themes").child(userId).child("mainColor");
        return RxFirebaseDatabase.getInstance(query)
                .onValueEvent()
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        return dataSnapshot.getValue(Integer.class);
                    }
                    return -1;
                });
    }

    @Override
    public Observable<String> observeConversationBackground(String userId, String conversationId) {
        Query query = database.getReference("conversations").child(userId).child(conversationId)
                .child("themes").child(userId).child("backgroundUrl");
        return RxFirebaseDatabase.getInstance(query)
                .onValueEvent()
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        return dataSnapshot.getValue(String.class);
                    }
                    return "";
                });
    }

    @Override
    public Observable<DataSnapshot> getDefaultBackgrounds() {
        Query query = database.getReference().child("backgrounds");
        return RxFirebaseDatabase.getInstance(query)
                .onSingleValueEvent()
                .toObservable();
    }

    @Override
    public Observable<Boolean> updateMaskOutput(String userId, String conversationId, Map<String, Boolean> memberIds, boolean mask) {
        Map<String, Object> updateValue = new HashMap<>();
        for (String id : memberIds.keySet()) {
            updateValue.put(String.format("conversations/%s/%s/maskOutputs/%s", userId, conversationId, id), mask);
        }
        return RxFirebaseDatabase.updateBatchData(database.getReference(), updateValue)
                .toObservable();
    }

    @Override
    public Observable<Map<String, String>> observeNicknames(String userId, String conversationId) {
        Query query = database.getReference().child(String.format("conversations/%s/%s/nickNames", userId, conversationId));
        return RxFirebaseDatabase.getInstance(query)
                .onValueEvent()
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        return (Map<String, String>)dataSnapshot.getValue();
                    }
                    return new HashMap<>();
                });
    }

    @Override
    public Observable<Boolean> createConversation(Conversation conversation) {
//        DatabaseReference groupReference = database.getReference("conversation");
//        return RxFirebaseDatabase.setValue(groupReference, group.toMap())
//                .map(reference -> true)
//                .toObservable();
        return null;
    }

    @Override
    public Observable<Boolean> updateConversation(String userId, String conversationId, Map<String, Object> values) {
        DatabaseReference reference = database.getReference(CHILD_CONVERSATION).child(userId).child(conversationId);
        return RxFirebaseDatabase.updateBatchData(reference, values)
                .toObservable();
    }
}
