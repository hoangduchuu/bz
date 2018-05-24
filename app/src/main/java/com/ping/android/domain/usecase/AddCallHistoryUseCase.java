package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.domain.usecase.message.SendCallMessageUseCase;
import com.ping.android.model.Call;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/19/18.
 */

public class AddCallHistoryUseCase extends UseCase<Boolean, Call> {
    @Inject
    UserRepository userRepository;
    @Inject
    SendCallMessageUseCase sendCallMessageUseCase;

    @Inject
    public AddCallHistoryUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Call call) {
        return userRepository.addCallHistory(call)
                .flatMap(aBoolean -> {
                    if (call.status == Constant.CALL_STATUS_SUCCESS || call.status == Constant.CALL_STATUS_MISS) {
                        SendCallMessageUseCase.Params params = new SendCallMessageUseCase.Params(call.opponentUser,
                                call.status == Constant.CALL_STATUS_MISS);
                        return sendCallMessageUseCase.buildUseCaseObservable(params)
                                .map(message -> true);
                    } else {
                        return Observable.just(true);
                    }
                });
    }
}
