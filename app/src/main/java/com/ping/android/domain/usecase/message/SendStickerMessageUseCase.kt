package com.ping.android.domain.usecase.message

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.data.mappers.MessageMapper
import com.ping.android.domain.repository.ConversationRepository
import com.ping.android.domain.repository.MessageRepository
import com.ping.android.domain.repository.StorageRepository
import com.ping.android.model.Conversation
import com.ping.android.model.Message
import com.ping.android.model.User
import com.ping.android.model.enums.MessageType
import io.reactivex.Observable
import java.io.File
import javax.inject.Inject

class SendStickerMessageUseCase @Inject constructor(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread
) : UseCase<Message, SendStickerMessageUseCase.Params>(threadExecutor, postExecutionThread) {
    @Inject lateinit var storageRepository: StorageRepository
    @Inject lateinit var sendMessageUseCase: SendMessageUseCase
    @Inject lateinit var conversationRepository: ConversationRepository
    @Inject lateinit var messageRepository: MessageRepository
    @Inject lateinit var messageMapper: MessageMapper

    override fun buildUseCaseObservable(params: Params): Observable<Message> {
        val builder = SendMessageUseCase.Params.Builder()
                .setMessageType(MessageType.STICKER)
                .setConversation(params.conversation)
                .setMarkStatus(params.maskStatus)
                .setCurrentUser(params.user)
                .setFileUrl(params.file)
        val cachedMessage = builder.build().message
        return conversationRepository.getMessageKey(params.conversation.key)
                .flatMap { messageKey ->
                    builder.setMessageKey(messageKey)
                    cachedMessage.key = messageKey
                    val temp = messageMapper.transform(cachedMessage, params.user)
                    temp.isCached = true
                    temp.localFilePath = params.file
                    temp.currentUserId = params.user.key
                    Observable.just(temp)
                            .concatWith(sendMessage(builder, params))
                }
    }

    private fun sendMessage(builder: SendMessageUseCase.Params.Builder, params: SendStickerMessageUseCase.Params): Observable<Message> {
        return sendMessageUseCase.buildUseCaseObservable(builder.build())
                .flatMap { message ->
                    storageRepository.uploadStickerFile(params.file)
                            .flatMap { stickerPath ->
                                messageRepository.updateImage(params.conversation.key, message.key, stickerPath)
                                        .map { message }
                            }
                }
    }

    data class Params(val file: String, val conversation: Conversation, val user: User, val maskStatus: Boolean)
}