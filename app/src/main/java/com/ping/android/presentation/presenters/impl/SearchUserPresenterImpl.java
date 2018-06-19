package com.ping.android.presentation.presenters.impl;

import android.text.TextUtils;

import com.ping.android.domain.usecase.SearchUsersUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.SearchUserPresenter;
import com.bzzzchat.cleanarchitecture.DefaultObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by tuanluong on 1/25/18.
 */

public class SearchUserPresenterImpl implements SearchUserPresenter {
    @Inject
    public SearchUsersUseCase searchUsersUseCase;
    @Inject
    public SearchUserPresenter.View view;

    private PublishSubject<String> querySubject;

    @Inject
    public SearchUserPresenterImpl() {
        querySubject = PublishSubject.create();
    }

    private void buildSearchUsers(Observable<String> query) {
        searchUsersUseCase.execute(new DefaultObserver<List<User>>() {
            @Override
            public void onNext(List<User> users) {
                super.onNext(users);
                view.hideSearching();
                view.displaySearchResult(users);
                if (users.size() > 0) {
                    view.hideNoResults();
                } else {
                    view.showNoResults();
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                super.onError(exception);
                exception.printStackTrace();
                view.displaySearchResult(new ArrayList<>());
                view.hideLoading();
                view.showNoResults();
            }
        }, query);
    }

    @Override
    public void searchUsers(String text) {
        if (TextUtils.isEmpty(text)) {
            view.displaySearchResult(new ArrayList<>());
        } else {
            view.hideNoResults();
            view.showSearching();
            querySubject.onNext(text);
        }
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
