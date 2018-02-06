package com.ping.android.data.repository;

import com.bzzz.rxquickblox.RxQuickblox;
import com.ping.android.domain.repository.QuickbloxRepository;
import com.quickblox.users.model.QBUser;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/6/18.
 */

public class QuickbloxRepositoryImpl implements QuickbloxRepository {

    @Inject
    public QuickbloxRepositoryImpl() {

    }

    @Override
    public Observable<QBUser> signIn(int qbId, String pingId) {
        return RxQuickblox.signIn(qbId, pingId);
    }

    @Override
    public Observable<QBUser> signUp(String pingId) {
        return RxQuickblox.signup(pingId);
    }
}
