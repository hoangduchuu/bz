package com.ping.android.domain.repository;

import com.bzzzchat.rxfirebase.events.ChildEvent;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface ConversationRepository {
    Observable<ChildEvent> registerConversationUpdate(String userId);
}
