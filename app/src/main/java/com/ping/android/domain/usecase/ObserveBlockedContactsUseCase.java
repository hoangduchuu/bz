package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.data.entity.ChildData;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/21/18.
 */

public class ObserveBlockedContactsUseCase extends UseCase<ChildData<User>, Void> {
    @Inject
    UserRepository userRepository;

    @Inject
    public ObserveBlockedContactsUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<ChildData<User>> buildUseCaseObservable(Void aVoid) {
        return userRepository.getCurrentUser()
                .flatMap(user -> userRepository.observeBlockedContacts(user.key)
                        .flatMap(childEvent -> {
                            String userId = childEvent.dataSnapshot.getKey();
                            return userRepository.getUser(userId)
                                    .map(user1 -> {
                                        ChildData<User> childData = new ChildData<>(user1, childEvent.type);
                                        return childData;
                                    });
                        })
                );
    }
}
