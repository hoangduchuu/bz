package com.ping.android.domain.usecase.message

import android.text.TextUtils
import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.data.mappers.MessageMapper
import com.ping.android.domain.repository.MessageRepository
import com.ping.android.model.Conversation
import com.ping.android.model.Message
import com.ping.android.model.User
import com.ping.android.utils.CommonMethod
import io.reactivex.Observable
import javax.inject.Inject

class GetUpdatedMessagesUseCase @Inject constructor(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread
) : UseCase<List<Message>, GetUpdatedMessagesUseCase.Params>(threadExecutor, postExecutionThread) {
    @Inject
    lateinit var messageRepository: MessageRepository
    @Inject
    lateinit var messageMapper: MessageMapper

    override fun buildUseCaseObservable(params: Params): Observable<List<Message>> {
        return messageRepository.getUpdatedMessages(params.conversation.key, params.timestamp)
                .map { entities ->
                    val user = params.user
                    val messages = ArrayList<Message>()
                    for (entity in entities) {
                        val message = messageMapper.transform(entity, user)
                        val isDeleted = CommonMethod.getBooleanFrom(entity.deleteStatuses, user.key)
                        val isReadable = entity.isReadable(user.key)
                        if (isDeleted || !isReadable) {
                            continue
                        }
                        val sender = getUser(message.senderId, params.conversation)
                        if (sender != null) {
                            message.senderProfile = sender!!.profile
                            message.senderName = if (TextUtils.isEmpty(sender!!.nickName)) sender!!.getDisplayName() else sender!!.nickName
                        }
                        messages.add(message)
                    }
                    return@map messages
                }
    }

    private fun getUser(userId: String, conversation: Conversation): User? {
        for (user in conversation.members) {
            if (userId == user.key) {
                return user
            }
        }
        return null
    }

    data class Params(val conversation: Conversation, val timestamp: Double, val user: User)
}