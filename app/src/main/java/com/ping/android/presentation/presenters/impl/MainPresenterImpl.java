package com.ping.android.presentation.presenters.impl;

import android.app.Activity;
import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.LoginQuickBloxUseCase;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.ObserveFriendsStatusUseCase;
import com.ping.android.domain.usecase.ObserveUsersChangedUseCase;
import com.ping.android.domain.usecase.RemoveUserBadgeUseCase;
import com.ping.android.domain.usecase.SyncMessageUseCase;
import com.ping.android.domain.usecase.conversation.GetConversationValueUseCase;
import com.ping.android.domain.usecase.user.ObserveBadgeCountUseCase;
import com.ping.android.domain.usecase.user.TurnOffMappingConfirmationUseCase;
import com.ping.android.domain.usecase.user.UpdateUserTransphabetUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.MainPresenter;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.utils.ActivityLifecycle;
import com.ping.android.utils.UsersUtils;
import com.ping.android.utils.bus.BusProvider;
import com.ping.android.utils.bus.events.BadgeCountUpdateEvent;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/10/18.
 */

public class MainPresenterImpl implements MainPresenter {
    @Inject
    View view;
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    @Inject
    ObserveFriendsStatusUseCase observeFriendsStatusUseCase;
    @Inject
    RemoveUserBadgeUseCase removeUserBadgeUseCase;
    @Inject
    LoginQuickBloxUseCase loginQuickBloxUseCase;
    @Inject
    TurnOffMappingConfirmationUseCase turnOffMappingConfirmationUseCase;
    @Inject
    UpdateUserTransphabetUseCase updateUserTransphabetUseCase;
    @Inject
    GetConversationValueUseCase getConversationValueUseCase;
    @Inject
    SyncMessageUseCase syncMessageUseCase;
    @Inject
    ObserveBadgeCountUseCase observeBadgeCountUseCase;
    @Inject
    ObserveUsersChangedUseCase observeUsersChangedUseCase;
    @Inject
    BusProvider busProvider;
    private User currentUser;
    private boolean isInit = false;

    @Inject
    public MainPresenterImpl() {

    }

    @Override
    public void create() {
        loginQuickBlox();
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                handleUserUpdate(user);
            }
        }, null);
        observeFriendsStatusUseCase.execute(new DefaultObserver<>(), null);
        syncMessageUseCase.execute(new DefaultObserver<>(), null);
        observeUsersChangedUseCase.execute(new DefaultObserver<>(), null);
    }

    private void handleUserUpdate(User user) {
        this.currentUser = user;
        if (!isInit) {
            if (TextUtils.isEmpty(currentUser.phone)) {
                view.openPhoneRequireView();
            }
            if (!currentUser.showMappingConfirm) {
                randomizeTransphabet(UsersUtils.randomizeEmoji());
                view.showMappingConfirm();
            }
            isInit = true;
            observeBadgeCount();
        }
    }

    @Override
    public void destroy() {
        observeBadgeCountUseCase.dispose();
        observeCurrentUserUseCase.dispose();
        observeFriendsStatusUseCase.dispose();
        syncMessageUseCase.dispose();
        observeUsersChangedUseCase.dispose();
    }

    @Override
    public void observeBadgeCount() {
        observeBadgeCountUseCase.execute(new DefaultObserver<Map<String, ? extends Integer>>() {
            @Override
            public void onNext(Map<String, ? extends Integer> stringMap) {
                if (stringMap.containsKey("refreshMock")) {
                    stringMap.remove("refreshMock");
                }
                Number missedCall = 0;
                if (stringMap.containsKey("missed_call")) {
                    missedCall = stringMap.get("missed_call");
                }
                stringMap.remove("missed_call");
                int messageCount = 0;
                Number count = 0;
                String currentChat = "";
                Activity activity = ActivityLifecycle.getInstance().getForegroundActivity();
                if (activity instanceof ChatActivity) {
                    currentChat = ((ChatActivity)activity).getConversationId();
                }
                stringMap.remove(currentChat);
                for (String key : stringMap.keySet()) {
                    count = stringMap.get(key);
                    messageCount += count.intValue();
                }
                busProvider.post(new BadgeCountUpdateEvent(new HashMap<>(stringMap), messageCount, missedCall.intValue()));
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, new Object());
    }

    @Override
    public void removeMissedCallsBadge() {
        removeUserBadgeUseCase.execute(new DefaultObserver<>(), "missed_call");
    }

    @Override
    public void onNetworkAvailable() {
        if (this.currentUser != null) {
            loginQuickBlox();
        }
    }

    @Override
    public void turnOffMappingConfirmation() {
        turnOffMappingConfirmationUseCase.execute(new DefaultObserver<>(), null);
    }

    @Override
    public void randomizeTransphabet(Map<String, String> maps) {
        updateUserTransphabetUseCase.execute(new DefaultObserver<>(), maps);
    }

    @Override
    public void handleNewConversation(String conversationId) {
        getConversationValueUseCase.execute(new DefaultObserver<Conversation>() {
            @Override
            public void onNext(Conversation conversation) {
                view.moveToChatScreen(conversation.key, conversation.currentColor);
            }
        }, conversationId);
    }

    private void loginQuickBlox() {
        loginQuickBloxUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.startCallService();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, null);
    }
}
