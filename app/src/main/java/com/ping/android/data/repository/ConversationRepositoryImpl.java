package com.ping.android.data.repository;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.ping.android.domain.repository.ConversationRepository;

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
}
