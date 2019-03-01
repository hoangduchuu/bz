package com.ping.android.domain.usecase.conversation

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.domain.repository.ConversationRepository
import com.ping.android.managers.UserManager
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class UpdateMaskOutputConversationUseCase @Inject constructor(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread
) : UseCase<Boolean, UpdateMaskOutputConversationUseCase.Params>(threadExecutor, postExecutionThread) {

    @Inject lateinit var conversationRepo: ConversationRepository
    @Inject lateinit var userManager: UserManager

    override fun buildUseCaseObservable(params: Params): Observable<Boolean> {
        return userManager.currentUser.flatMap {
            conversationRepo.updateMaskOutput(it.key, params.conversationId, params.memberIds, params.isMask).toObservable()
        }
    }

    data class Params(
            var conversationId: String,
            var memberIds: Map<String, Boolean>,
            var isMask: Boolean
    )
}