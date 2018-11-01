package com.ping.android.presentation.presenters.impl;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.data.entity.ChildData;
import com.ping.android.domain.usecase.ObserveFriendsChildEventUseCase;
import com.ping.android.domain.usecase.SearchUsersUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.SearchUserPresenter;
import com.ping.android.utils.bus.Variable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by tuanluong on 1/25/18.
 */

public class SearchUserPresenterImpl implements SearchUserPresenter {
    @Inject
    public SearchUsersUseCase searchUsersUseCase;
    @Inject
    ObserveFriendsChildEventUseCase observeFriendsChildEventUseCase;
    @Inject
    public SearchUserPresenter.View view;

    private Variable<String> friendValue;
    private List<User> friends;
    private PublishSubject<String> querySubject;
    private String searchTerm = "";
    private CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public SearchUserPresenterImpl() {
        querySubject = PublishSubject.create();
        friends = new ArrayList<>();
        friendValue = new Variable<>("");
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

            @Override
            public void onComplete() {
                super.onComplete();
            }
        }, query);
    }

    @Override
    public void searchUsers(String text) {
        searchTerm = text;
        if (TextUtils.isEmpty(text)) {
            view.displaySearchResult(friends);
        } else {
            view.hideNoResults();
            view.showSearching();
            querySubject.onNext(text);
        }
    }

    @Override
    public void create() {
        buildSearchUsers(querySubject.share());
        friendValue.asObservable()
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<String>() {
                    @Override
                    public void onNext(String value) {
                        if (TextUtils.isEmpty(searchTerm)) {
                            view.hideNoResults();
                            view.displaySearchResult(friends);
                        }
                    }
                });
        observeFriendsChildEventUseCase.execute(new DefaultObserver<ChildData<User>>() {
            @Override
            public void onNext(ChildData<User> userChildData) {
                switch (userChildData.getType()) {
                    case CHILD_ADDED:
                        friends.add(userChildData.getData());
                        friendValue.set(userChildData.getData().key);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, null);
    }

    @Override
    public void destroy() {
        view = null;
        searchUsersUseCase.dispose();
    }
}
