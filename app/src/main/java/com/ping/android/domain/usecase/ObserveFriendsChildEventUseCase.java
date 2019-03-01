package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.entity.ChildData;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import durdinapps.rxfirebase2.RxFirebaseChildEvent;
import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/14/18.
 */

public class ObserveFriendsChildEventUseCase extends UseCase<ChildData<User>, Void> {
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    @Inject
    public ObserveFriendsChildEventUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<ChildData<User>> buildUseCaseObservable(Void aVoid) {
        return userManager.getCurrentUser()
                .flatMap(user -> userRepository.observeFriendsChildEvent(user.key)
                        .flatMap(childEvent -> {
                            String friendId = childEvent.getValue().getKey();
                            boolean value = childEvent.getValue().getValue(Boolean.class);
                            if ((childEvent.getEventType() == RxFirebaseChildEvent.EventType.ADDED
                                    || (childEvent.getEventType() == RxFirebaseChildEvent.EventType.CHANGED && value)) && !user.key.equals(friendId)) {
                                return getUser(friendId)
                                        .map(friend -> {
                                            ChildData<User> childData = new ChildData(friend, childEvent.getEventType());
                                            return childData;
                                        });
                            } else {
                                User user1 = new User();
                                user1.key = friendId;
                                ChildData<User> childData = new ChildData(user1, RxFirebaseChildEvent.EventType.REMOVED);
                                return Observable.just(childData);
                            }
                        })
                );
    }

    private Observable<User> getUser(String userId) {
        return userRepository.getUser(userId);
    }
}
