package com.ping.android.domain.usecase.user

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.domain.repository.UserRepository
import io.reactivex.Observable
import javax.inject.Inject

class CheckPasswordUseCase @Inject constructor(threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
        UseCase<Boolean, String>(threadExecutor, postExecutionThread) {
    @Inject lateinit var userRepository: UserRepository

    override fun buildUseCaseObservable(params: String): Observable<Boolean> {
       return userRepository.checkPassword(params)
    }


}