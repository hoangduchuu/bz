package com.ping.android.domain.usecase

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.data.entity.ChildData
import com.ping.android.domain.repository.UserRepository
import com.ping.android.managers.UserManager
import com.ping.android.model.User
import io.reactivex.Observable
import javax.inject.Inject

class ObserveUsersChangedUseCase @Inject constructor(
        threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
): UseCase<User, Void?>(threadExecutor, postExecutionThread) {
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var userManager: UserManager

    override fun buildUseCaseObservable(params: Void?): Observable<User> {
        return userRepository.observeUsersChanged()
                .map { user ->
                    userManager.userUpdated(user)
                    user
                }
    }
}