package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.ChildData;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/14/18.
 */

public class ObserveFriendsChildEventUseCase extends UseCase<ChildData<User>, Void> {
    @Inject
    UserRepository userRepository;

    @Inject
    public ObserveFriendsChildEventUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<ChildData<User>> buildUseCaseObservable(Void aVoid) {
        return userRepository.getCurrentUser()
                .flatMap(user -> userRepository.observeFriendsChildEvent(user.key)
                        .flatMap(childEvent -> {
                            String friendId = childEvent.dataSnapshot.getKey();
                            boolean value = childEvent.dataSnapshot.getValue(Boolean.class);
                            if (childEvent.type == ChildEvent.Type.CHILD_ADDED
                                    || (childEvent.type == ChildEvent.Type.CHILD_CHANGED && value)) {
                                return getUser(friendId)
                                        .map(friend -> {
                                            ChildData<User> childData = new ChildData(friend, childEvent.type);
                                            return childData;
                                        });
                            } else {
                                User user1 = new User();
                                user1.key = friendId;
                                ChildData<User> childData = new ChildData(user1, ChildEvent.Type.CHILD_REMOVED);
                                return Observable.just(childData);
                            }
                        })
                );
    }

    private Observable<User> getUser(String userId) {
        return userRepository.getUser(userId);
    }
}
