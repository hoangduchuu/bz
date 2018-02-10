package com.ping.android.data.repository;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ConversationRepositoryImpl implements ConversationRepository {
    FirebaseDatabase database;

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
    public Observable<ChildEvent> registerConversationsUpdate(String userId) {
        Query query = database.getReference("conversations")
                .child(userId)
                .orderByChild("timesstamps");
        return RxFirebaseDatabase.getInstance(query).onChildEvent();
    }

    @Override
    public Observable<DataSnapshot> observeConversationValue(String conversationId) {
        Query query = database.getReference("conversations").child(conversationId);
        return RxFirebaseDatabase.getInstance(query).onValueEvent();
    }

    @Override
    public Observable<String> getMessageKey() {
        String key = database.getReference("messages").push().getKey();
        return Observable.just(key);
    }

    @Override
    public Observable<Message> sendMessage(String conversationId, Message message) {
        DatabaseReference reference = database.getReference("messages").child(conversationId).child(message.key);
        return RxFirebaseDatabase.setValue(reference, message.toMap())
                .map(reference1 -> message)
                .toObservable();
    }

    @Override
    public Observable<Boolean> createConversation(Conversation conversation) {
//        DatabaseReference groupReference = database.getReference("conversation");
//        return RxFirebaseDatabase.setValue(groupReference, group.toMap())
//                .map(reference -> true)
//                .toObservable();
        return null;
    }
}
