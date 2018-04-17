package com.ping.android.data.repository;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.bzzz.rxquickblox.RxJava2PerformProcessor;
import com.bzzz.rxquickblox.RxQuickblox;
import com.ping.android.domain.repository.QuickbloxRepository;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.server.Performer;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;

import org.jivesoftware.smack.SmackException;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

/**
 * Created by tuanluong on 2/6/18.
 */

public class QuickbloxRepositoryImpl implements QuickbloxRepository {
    private static final String TAG = RxQuickblox.class.getSimpleName();
    public static final String QB_PING_ROOM = "mnb";
    public static final String DEFAULT_USER_PASSWORD = "x6Bt0VDy5";

    @Inject
    public QuickbloxRepositoryImpl() {
    }

    @Override
    public Observable<QBUser> signIn(int qbId, String pingId) {
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

    @Override
    public Observable<QBUser> getUser(String pingId) {
        Performer<QBUser> performer = QBUsers.getUserByLogin(pingId);
        return ((Observable<QBUser>) performer.convertTo(RxJava2PerformProcessor.INSTANCE))
                .map(qbUser -> {
                    qbUser.setPassword(DEFAULT_USER_PASSWORD);
                    return qbUser;
                });
    }

    @Override
    public Observable<QBUser> signUp(String pingId) {
        QBUser qbUser = new QBUser();

        qbUser.setLogin(pingId);
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

    @Override
    public Observable<Boolean> loginChat(int qbId, String pingId) {
        return Observable.create(emitter -> {
            QBUser user = getQBUser(qbId, pingId);
            QBChatService.getInstance().login(user, new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
                    emitter.onNext(true);
                }

                @Override
                public void onError(QBResponseException e) {
                    emitter.onError(e);
                }
            });
        });
    }

    @Override
    public Observable<Boolean> logout() {
        try {
            QBChatService.getInstance().logout();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return Observable.just(true);
    }

    private QBUser getQBUser(int qbId, String pingId) {
        QBUser qbUser = new QBUser();
        //StringifyArrayList<String> userTags = new StringifyArrayList<>();
        //userTags.add(QB_PING_ROOM);
        qbUser.setId(qbId);
        qbUser.setLogin(pingId);
        qbUser.setPassword(DEFAULT_USER_PASSWORD);
        return qbUser;
    }
}
