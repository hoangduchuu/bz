package com.ping.android.domain.usecase.conversation

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.domain.repository.ConversationRepository
import com.ping.android.domain.repository.UserRepository
import io.reactivex.Observable
import javax.inject.Inject

class UpdateMaskOutputConversationUseCase @Inject constructor(
        private val threadExecutor: ThreadExecutor,
        private val postExecutionThread: PostExecutionThread
) : UseCase<Boolean, UpdateMaskOutputConversationUseCase.Params>(threadExecutor, postExecutionThread) {

    @Inject lateinit var conversationRepo: ConversationRepository
    @Inject lateinit var userRepository: UserRepository

    override fun buildUseCaseObservable(params: Params): Observable<Boolean> {
        return userRepository.currentUser.flatMap {
            conversationRepo.updateMaskOutput(it.key, params.conversationId, params.memberIds, params.isMask)
        }
    }

    data class Params(
            var conversationId: String,
            var memberIds: Map<String, Boolean>,
            var isMask: Boolean
    )
}