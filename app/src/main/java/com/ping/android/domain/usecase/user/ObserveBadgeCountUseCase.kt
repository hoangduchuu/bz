package com.ping.android.domain.usecase.user

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.domain.repository.UserRepository
import com.ping.android.managers.UserManager
import io.reactivex.Observable
import javax.inject.Inject

class ObserveBadgeCountUseCase @Inject constructor(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread
) : UseCase<Map<String, Int>, Any>(threadExecutor, postExecutionThread) {

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var userManager: UserManager

    override fun buildUseCaseObservable(params: Any): Observable<Map<String, Int>> {
           return userManager.currentUser.flatMap { user ->
               userRepository.observeBadgeCount(user.key)
           }
    }
}