package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.SearchRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.User;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/23/18.
 */

public class SearchUsersUseCase extends UseCase<List<User>, Observable<String>> {
    @Inject
    SearchRepository searchRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public SearchUsersUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<List<User>> buildUseCaseObservable(Observable<String> textQuery) {
        return textQuery
                .map(query -> {
                    String[] queries = query.split(" ");
                    StringBuilder newQuery = new StringBuilder();
                    for (String q : queries) {
                        if (!q.isEmpty()) {
                            newQuery.append(q).append("*");
                        }
                    }
                    return newQuery.toString();
                })
                .switchMap(query -> searchRepository.searchUsers(query)
                        .flatMap(users -> {
                            if (users.size() > 0) {
                                return userRepository.getCurrentUser()
                                        .map(user -> {
                                            for (User u : users) {
                                                // TODO
                                                u.typeFriend = user.friends.containsKey(u.key)
                                                        ? Constant.TYPE_FRIEND.IS_FRIEND : Constant.TYPE_FRIEND.NON_FRIEND;
                                            }
                                            return users;
                                        });
                            } else {
                                return Observable.just(users);
                            }
                        })
                );
    }
}
