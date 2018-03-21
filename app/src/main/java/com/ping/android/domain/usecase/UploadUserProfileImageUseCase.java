package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

public class UploadUserProfileImageUseCase extends UseCase<Boolean, String> {
    @Inject
    StorageRepository storageRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public UploadUserProfileImageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(String filePath) {
        return userRepository.getCurrentUser()
                .flatMap(user -> storageRepository.uploadUserProfileImage(user.key, filePath)
                        .flatMap(s -> userRepository.updateUserProfileImage(user.key, s)));
    }
}
