package com.ping.android.domain.repository;

import com.bzzzchat.rxfirebase.database.ChildEvent;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/26/18.
 */

public interface MessageRepository {
    Observable<ChildEvent> observeMessageUpdate(String conversationId);
}
