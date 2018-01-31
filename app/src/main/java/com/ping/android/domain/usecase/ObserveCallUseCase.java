package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.rxfirebase.events.ChildEvent;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;
import com.ping.android.model.ChildData;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 1/30/18.
 */

public class ObserveCallUseCase extends UseCase<ChildData<Call>, Void> {
    @Inject
    UserRepository userRepository;
    UserManager userManager;

    @Inject
    public ObserveCallUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        userManager = UserManager.getInstance();
    }

    @NotNull
    @Override
    public Observable<ChildData<Call>> buildUseCaseObservable(Void aVoid) {
        String userId = userManager.getUser().key;
        return userRepository.getCalls(userId)
                .flatMap(childEvent -> {
                    Call call = Call.from(childEvent.dataSnapshot);
                    ChildEvent.Type type = call.deleteStatuses.containsKey(userId) && call.deleteStatuses.get(userId)
                            ? ChildEvent.Type.CHILD_REMOVED : ChildEvent.Type.CHILD_ADDED;
                    if (type == ChildEvent.Type.CHILD_ADDED) {
                        return userRepository.getUser(call.senderId)
                                .map(user -> {
                                    call.opponentUser = user;
                                    call.members.add(user);
                                    call.members.add(userManager.getUser());
                                    return new ChildData<>(call, type);
                                });
                    } else {
                        return Observable.just(new ChildData<>(call, type));
                    }
                });
    }
}
