package com.ping.android.presentation.presenters.impl;

import com.ping.android.domain.usecase.SearchUsersUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.NewChatPresenter;
import com.tl.cleanarchitecture.DefaultObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by tuanluong on 1/22/18.
 */

public class NewChatPresenterImpl implements NewChatPresenter {
    @Inject
    public NewChatView view;
    @Inject
    public SearchUsersUseCase searchUsersUseCase;
    private PublishSubject<String> querySubject;

    @Inject
    public NewChatPresenterImpl() {
        querySubject = PublishSubject.create();
    }

    private void buildSearchUsers(Observable<String> query) {
        searchUsersUseCase.execute(new DefaultObserver<List<User>>() {
            @Override
            public void onNext(List<User> users) {
                super.onNext(users);
                view.displaySearchResult(users);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                super.onError(exception);
                exception.printStackTrace();
                view.displaySearchResult(new ArrayList<>());
            }
        }, query);
    }

    @Override
    public void searchUsers(String text) {
        querySubject.onNext(text);
    }

    @Override
    public void create() {
        buildSearchUsers(querySubject.share());
    }

    @Override
    public void destroy() {
        searchUsersUseCase.dispose();
    }
}
