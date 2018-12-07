package com.ping.android.domain.usecase.message

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.data.entity.MessageEntity
import com.ping.android.domain.repository.ConversationRepository
import com.ping.android.domain.repository.MessageRepository
import com.ping.android.model.Message

import javax.inject.Inject

import io.reactivex.Observable

/**
 * Created by Huu Hoang on 05/12/2018
 */

class SendNewStickerMessageUseCase @Inject
constructor(threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) : UseCase<Message, SendMessageUseCase.Params>(threadExecutor, postExecutionThread) {
    @Inject
    lateinit var conversationRepository: ConversationRepository
    @Inject
    lateinit var sendMessageUseCase: SendMessageUseCase

    @Inject
    lateinit var messageRepository: MessageRepository

    override fun buildUseCaseObservable(params: SendMessageUseCase.Params): Observable<Message> {

        return conversationRepository.getMessageKey(params.conversation.key)
                .map<MessageEntity> { s ->
                    params.setMessageKey(s)
                    params.message
                }
                .flatMap { message -> sendMessageUseCase.buildUseCaseObservable(params) }
                .flatMap { message ->
                    messageRepository.markSenderMessageStatusAsDelivered(params.conversation.key, message.key, message.currentUserId, message.mediaUrl)
                            .map { s -> message }
                }


    }
}
