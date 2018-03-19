package com.bzzz.rxquickblox;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.Performer;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

/**
 * Created by tuanluong on 2/6/18.
 */

public class RxQuickblox {
    private static final String TAG = RxQuickblox.class.getSimpleName();
    public static final String QB_PING_ROOM = "mnb";
    public static final String DEFAULT_USER_PASSWORD = "x6Bt0VDy5";

    public static Observable<QBUser> signIn(int qbId, String pingId) {
        QBUser qbUser = getQBUser(qbId, pingId);

        //	creating QBUsers.signIn performer
        Performer<QBUser> performer = QBUsers.signIn(qbUser);

        return ((Observable<QBUser>) performer.convertTo(RxJava2PerformProcessor.INSTANCE))
                .map(new Function<QBUser, QBUser>() {
                    @Override
                    public QBUser apply(QBUser qbUser) throws Exception {
                        qbUser.setPassword(DEFAULT_USER_PASSWORD);
                        return qbUser;
                    }
                });
    }

    public static Observable<QBUser> signup(String pingID) {
        QBUser qbUser = new QBUser();

        qbUser.setLogin(pingID);
        qbUser.setPassword(DEFAULT_USER_PASSWORD);
        Performer<QBUser> performer = QBUsers.signUp(qbUser);
        return ((Observable<QBUser>) performer.convertTo(RxJava2PerformProcessor.INSTANCE))
                .map(new Function<QBUser, QBUser>() {
                    @Override
                    public QBUser apply(QBUser qbUser) throws Exception {
                        qbUser.setPassword(DEFAULT_USER_PASSWORD);
                        return qbUser;
                    }
                });
    }

    private static QBUser getQBUser(int qbId, String pingId) {
        QBUser qbUser = new QBUser();
        //StringifyArrayList<String> userTags = new StringifyArrayList<>();
        //userTags.add(QB_PING_ROOM);
        qbUser.setId(qbId);
        qbUser.setLogin(pingId);
        qbUser.setPassword(DEFAULT_USER_PASSWORD);
        return qbUser;
    }

    private static Observable<Boolean> loginChat(final int qbId, final String pingId) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> emitter) throws Exception {
                QBUser user = getQBUser(qbId, pingId);
                QBChatService.getInstance().login(user, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Log.d(TAG, "login onSuccess");
                        emitter.onNext(true);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        emitter.onError(e);
                    }
                });
            }
        });
    }
}
