package com.ping.android.domain.repository;

import com.quickblox.users.model.QBUser;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/6/18.
 */

public interface QuickbloxRepository {
    Observable<QBUser> signIn(int qbId, String pingId);

    Observable<QBUser> signUp(String pingId);

    Observable<Boolean> loginChat(int qbId, String pingId);

    Observable<Boolean> logout();
}
