package com.ping.android.domain.usecase.user

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.data.entity.ChildData
import com.ping.android.domain.repository.UserRepository
import com.ping.android.domain.usecase.conversation.GetConversationValueUseCase
import com.ping.android.managers.UserManager
import com.ping.android.model.Badge
import com.ping.android.utils.CommonMethod
import io.reactivex.Observable
import javax.inject.Inject

class ObserveBadgeCountUseCase @Inject constructor(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread
) : UseCase<Map<String, Int>, Any>(threadExecutor, postExecutionThread) {

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var userManager: UserManager
    @Inject lateinit var getConversationUseCase: GetConversationValueUseCase

    override fun buildUseCaseObservable(params: Any): Observable<Map<String, Int>> {
           return userManager.currentUser.flatMap { user ->
               userRepository.observeBadgeCountChildEvent(user.key)
                       .flatMap {
                           if (it.type  == ChildData.Type.CHILD_ADDED || it.type  == ChildData.Type.CHILD_CHANGED) {
                               if (it.data.key == "refreshMock" || it.data.key == "missed_call") {
                                   Observable.just(updateBadge(it.data))
                               } else {
                                   getConversationUseCase.buildUseCaseObservable(it.data.key)
                                           .map { conversation ->
                                               val isRead = CommonMethod.getBooleanFrom(conversation.readStatuses, user.key)
                                               if (isRead) {
                                                   val badge = it.data
                                                   badge.value = 0
                                                   updateBadge(badge)
                                               } else {
                                                   updateBadge(it.data)
                                               }
                                           }
                               }
                           } else {
                               val badge = it.data
                               badge.value = 0
                               Observable.just(updateBadge(badge))
                           }
                       }
           }
    }

    private fun updateBadge(badge: Badge): Map<String, Int> {
        return userManager.updateBadge(badge.key, badge.value)
    }
}