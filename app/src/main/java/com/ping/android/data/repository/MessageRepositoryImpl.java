package com.ping.android.data.repository;

import com.bzzzchat.rxfirebase.RxFirebaseDatabase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
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
    public Observable<ChildEvent> observeMessageUpdate(String conversationId) {
        Query query = database.getReference("messages").child(conversationId)
                .orderByChild("timestamp")
                .limitToLast(Constant.LATEST_RECENT_MESSAGES);
        return RxFirebaseDatabase.getInstance(query)
                .onChildEvent();
    }
}
