package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.CheckAppUpdateUseCase;
import com.ping.android.domain.usecase.GetCurrentUserUseCase;
import com.ping.android.domain.usecase.InitializeUserUseCase;
import com.ping.android.domain.usecase.conversation.GetConversationValueUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.SplashPresenter;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/6/18.
 */

public class SplashPresenterImpl implements SplashPresenter {
    @Inject
    View view;
    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;
    @Inject
    GetConversationValueUseCase getConversationValueUseCase;
    @Inject
    InitializeUserUseCase initializeUserUseCase;
    @Inject
    CheckAppUpdateUseCase checkAppUpdateUseCase;
    private AtomicInteger initializeSteps;
    private boolean isLoggedIn = false;

    @Inject
    public SplashPresenterImpl() {
        initializeSteps = new AtomicInteger(3);
    }

    @Override
    public void create() {
        checkAppUpdateUseCase.execute(new DefaultObserver<CheckAppUpdateUseCase.Output>() {
            @Override
            public void onNext(CheckAppUpdateUseCase.Output output) {
                if (output.needUpdate) {
                    view.showAppUpdateDialog(output.appId, output.currentVersion);
                } else {
                    onStepFinish();
                }
            }
        }, null);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                finishTimer();
            }
        };
        timer.schedule(task, 3000);
    }

    @Override
    public void initializeUser() {
        initializeUserUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                isLoggedIn = aBoolean;
                onStepFinish();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                onStepFinish();
            }
        }, null);
    }

    @Override
    public void finishTimer() {
        onStepFinish();
    }

    @Override
    public void handleNewConversation(String conversationId) {
        getCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                view.navigateToMainScreenWithExtra(conversationId);
            }
        }, null);

//        getConversationValueUseCase.execute(new DefaultObserver<Conversation>() {
//            @Override
//            public void onNext(Conversation conversation) {
//                view.navigateToMainScreenWithExtra(conversation.key, conversation.currentColor);
//            }
//
//            @Override
//            public void onError(@NotNull Throwable exception) {
//                exception.printStackTrace();
//            }
//        }, conversationId);
    }

    private void onStepFinish() {
        if (initializeSteps.decrementAndGet() == 0) {
            if (isLoggedIn) {
                view.startCallService();
                view.navigateToMainScreen();
            } else {
                view.navigateToLoginScreen();
            }
        }
    }

    @Override
    public void destroy() {
        view = null;
        initializeUserUseCase.dispose();
        getCurrentUserUseCase.dispose();
        getConversationValueUseCase.dispose();
        checkAppUpdateUseCase.dispose();
    }
}
