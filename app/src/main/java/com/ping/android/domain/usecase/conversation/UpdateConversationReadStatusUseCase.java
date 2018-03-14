package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/13/18.
 */

public class UpdateConversationReadStatusUseCase extends UseCase<Boolean, Conversation> {
    @Inject
    UserRepository userRepository;
    @Inject
    ConversationRepository conversationRepository;

    @Inject
    public UpdateConversationReadStatusUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Conversation conversation) {
        return userRepository.getCurrentUser()
                .flatMap(user -> conversationRepository.updateReadStatus(conversation.key, user.key));
    }
}
