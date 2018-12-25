package com.ping.android.domain.usecase.user

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.domain.repository.UserRepository
import com.ping.android.model.User
import io.reactivex.Observable
import java.util.ArrayList
import javax.inject.Inject

class GetUsersProfileFromUserIdsUseCase @Inject constructor(threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
        UseCase<MutableList<User>, GetUsersProfileFromUserIdsUseCase.Params>(threadExecutor, postExecutionThread) {
    @Inject
    lateinit var userRepository: UserRepository

    /**
     * Builds an {@link Observable} which will be used when executing the current {@link UseCase}.
     */
    override fun buildUseCaseObservable(params: GetUsersProfileFromUserIdsUseCase.Params): Observable<MutableList<User>> {
        return userRepository.getUsersProfileInformationFromUserIds(params.userIds)
    }


    class Params( val userIds: ArrayList<String>)


}