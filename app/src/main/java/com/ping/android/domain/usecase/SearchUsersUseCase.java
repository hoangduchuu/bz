package com.ping.android.domain.usecase;

import com.ping.android.domain.repository.SearchRepository;
import com.ping.android.model.User;
import com.tl.cleanarchitecture.PostExecutionThread;
import com.tl.cleanarchitecture.ThreadExecutor;
import com.tl.cleanarchitecture.UseCase;

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
    public SearchUsersUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<List<User>> buildUseCaseObservable(Observable<String> textQuery) {
        return textQuery.flatMap(query -> searchRepository.searchUsers(query));
    }
}
