package com.ping.android.domain.usecase.user

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.domain.repository.UserRepository
import com.ping.android.model.User
import io.reactivex.Observable
import java.util.ArrayList
import javax.inject.Inject

class GetUsersProfileUseCaseFromUserIds @Inject constructor(threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
        UseCase<MutableList<User>, GetUsersProfileUseCaseFromUserIds.Params>(threadExecutor, postExecutionThread) {
    @Inject
    lateinit var userRepository: UserRepository

    /**
     * Builds an {@link Observable} which will be used when executing the current {@link UseCase}.
     */
    override fun buildUseCaseObservable(params: GetUsersProfileUseCaseFromUserIds.Params): Observable<MutableList<User>> {
        return userRepository.getUsersProfileInfomationFromUserIds(params.users)
    }


    class Params(val users: ArrayList<String>)


}