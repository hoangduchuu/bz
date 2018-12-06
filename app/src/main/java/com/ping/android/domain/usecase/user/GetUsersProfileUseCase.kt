package com.ping.android.domain.usecase.user

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.domain.repository.UserRepository
import com.ping.android.model.User
import io.reactivex.Observable
import java.util.ArrayList
import javax.inject.Inject

class GetUsersProfileUseCase @Inject constructor(threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
        UseCase<MutableList<User>, GetUsersProfileUseCase.Params>(threadExecutor, postExecutionThread) {
    @Inject
    lateinit var userRepository: UserRepository

    /**
     * Builds an {@link Observable} which will be used when executing the current {@link UseCase}.
     */
    override fun buildUseCaseObservable(params: GetUsersProfileUseCase.Params): Observable<MutableList<User>> {
        return userRepository.getUsersProfileInfomation(params.users)
    }


    class Params(val users: ArrayList<User>)


}