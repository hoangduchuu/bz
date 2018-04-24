package com.ping.android.domain.usecase.conversation;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.domain.repository.ConversationRepository;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

public class GetDefaultBackgroundsUseCase extends UseCase<List<String>, Void> {
    @Inject
    ConversationRepository conversationRepository;

    @Inject
    public GetDefaultBackgroundsUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<List<String>> buildUseCaseObservable(Void aVoid) {
        return conversationRepository.getDefaultBackgrounds()
                .map(dataSnapshot -> {
                    List<String> result = new ArrayList<>();
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            result.add(child.getValue(String.class));
                        }
                    }
                    return result;
                });
    }
}
