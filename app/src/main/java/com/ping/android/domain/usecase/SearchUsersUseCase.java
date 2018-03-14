package com.ping.android.domain.usecase;

import com.ping.android.domain.repository.SearchRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.ultility.Constant;
import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;

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
    UserManager userManager;

    @Inject
    public SearchUsersUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        userManager = UserManager.getInstance();
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
                .flatMap(query -> searchRepository.searchUsers(query))
                .zipWith(userRepository.getCurrentUser(), (users, user) -> {
                    User currentUser = userManager.getUser();
                    for (User u : users) {
                        // TODO
                        u.typeFriend = currentUser.friends.containsKey(user.key) ? Constant.TYPE_FRIEND.IS_FRIEND : Constant.TYPE_FRIEND.NON_FRIEND;
                    }
                    return users;
                });
    }
}
