package com.ping.android.domain.usecase.call;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Init call information.
 * This use case will get current user info with nickname if have any.
 *
 * Created by tuanluong on 3/20/18.
 */
public class InitCallInfoUseCase extends UseCase<InitCallInfoUseCase.Output, String> {
    @Inject
    UserRepository userRepository;
    @Inject
    ConversationRepository conversationRepository;

    @Inject
    public InitCallInfoUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Output> buildUseCaseObservable(String s) {
        return userRepository.getCurrentUser()
                .flatMap(user -> {
                    String conversationID = user.key.compareTo(s) > 0 ? user.key + s : s + user.key;
                    return conversationRepository.getConversationNickName(user.key, conversationID, s)
                            .zipWith(conversationRepository.getConversationNickName(s, conversationID, user.key), (s1, s2) -> {
                                Output output = new Output();
                                output.opponentNickname = s1;
                                output.currentUserNickname = s2;
                                output.currentUser = user;
                                return output;
                            });
                });
    }

    public static class Output {
        public String opponentNickname;
        public User currentUser;
        public String currentUserNickname;
    }
}
