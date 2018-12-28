package com.ping.android.domain.usecase

import android.text.TextUtils
import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.data.entity.MessageEntity
import com.ping.android.data.mappers.MessageMapper
import com.ping.android.domain.repository.CommonRepository
import com.ping.android.domain.repository.ConversationRepository
import com.ping.android.domain.repository.MessageRepository
import com.ping.android.domain.repository.StorageRepository
import com.ping.android.domain.usecase.conversation.GetConversationValueUseCase
import com.ping.android.domain.usecase.notification.SendMessageNotificationUseCase
import com.ping.android.model.enums.MessageType
import com.ping.android.utils.Utils
import com.ping.android.utils.configs.Constant
import io.reactivex.Observable
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncMessageUseCase @Inject constructor(
        threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : UseCase<Boolean, Void?>(threadExecutor, postExecutionThread) {

    @Inject
    lateinit var commonRepository: CommonRepository
    @Inject
    lateinit var messageRepository: MessageRepository
    @Inject
    lateinit var conversationRepository: ConversationRepository
    @Inject
    lateinit var storageRepository: StorageRepository
    @Inject
    lateinit var getConversationValueUseCase: GetConversationValueUseCase
    @Inject
    lateinit var sendMessageNotificationUseCase: SendMessageNotificationUseCase
    @Inject
    lateinit var messageMapper: MessageMapper

    override fun buildUseCaseObservable(params: Void?): Observable<Boolean> {
        return commonRepository.observeConnectionState()
                .flatMap { isConnected ->
                    if (isConnected) {
                        Observable.timer(2, TimeUnit.SECONDS)
                                .flatMap {
                                    messageRepository.errorMessages
                                            .flatMap { messages ->
                                                val imageMessages = messages.filter {
                                                    it.messageType in arrayOf(Constant.MSG_TYPE_IMAGE, Constant.MSG_TYPE_GAME)
                                                }
                                                val textMessages = messages.filter {
                                                    it.messageType == Constant.MSG_TYPE_TEXT
                                                }
                                                val groupImageMessages = messages.filter {
                                                    it.messageType == Constant.MSG_TYPE_IMAGE_GROUP
                                                }
                                                val audioMessages = messages.filter {
                                                    it.messageType in arrayOf(Constant.MSG_TYPE_VOICE, Constant.MSG_TYPE_VIDEO)
                                                }
                                                resendTextMessage(textMessages)
                                                        .concatWith(resendImageMessages(ArrayList(imageMessages)))
                                                        .concatWith(resendGroupImageMessages(ArrayList(groupImageMessages)))
                                                        .concatWith(resendFileMessages(ArrayList(audioMessages)))
                                            }
                                }
                    } else {
                        Observable.just(true)
                    }
                }
    }

    private fun resendTextMessage(messages: List<MessageEntity>): Observable<Boolean> {
        if (messages.isEmpty()) return Observable.just(true)
        val updateValue = HashMap<String, Any>()
        for (entity in messages) {
            updateValue["messages/${entity.conversationId}/${entity.key}/status/${entity.senderId}"] = Constant.MESSAGE_STATUS_DELIVERED
            updateValue["messages/${entity.conversationId}/${entity.key}/updateAt"] = System.currentTimeMillis()/1000.0
        }
        return commonRepository.updateBatchData(updateValue)
                .concatWith(Observable.just(messages)
                        .flatMapIterable { list -> list }
                        .flatMap { message ->
                            messageRepository.updateLocalMessageStatus(message.key, Constant.MESSAGE_STATUS_DELIVERED)
                            getConversationValueUseCase.buildUseCaseObservable(message.conversationId)
                                    .flatMap { conversation ->
                                        sendMessageNotificationUseCase.buildUseCaseObservable(SendMessageNotificationUseCase.Params(conversation, message.key, message.message, MessageType.TEXT))
                                    }
                        }
                )
    }

    @Suppress("LABEL_NAME_CLASH")
    private fun resendGroupImageMessages(messages: ArrayList<MessageEntity>): Observable<Boolean> {
        if (messages.isEmpty()) return Observable.just(true)
        return Observable.just(messages)
                .flatMapIterable { list -> list }
                .flatMap { message ->
                    Observable.just(message.childMessages)
                            .flatMapIterable { child -> child }
                            .flatMap { childEntity ->
                                val localFile = File(childEntity.photoUrl)
                                if (!localFile.exists()) return@flatMap Observable.just(true)
                                uploadThumbnail(message.conversationId, localFile.absolutePath)
                                        .concatMap { thumbUrl ->
                                            uploadImage(message.conversationId, localFile.absolutePath)
                                                    .flatMap { photoUrl ->
                                                        messageRepository.updateChildMessageImage(message.conversationId,
                                                                message.key, childEntity.key, thumbUrl, photoUrl)
                                                    }
                                        }
                            }
                            .take(message.childMessages.size.toLong())
                            .toList()
                            .toObservable()
                            .flatMap {
                                messageRepository.updateMessageStatus(message.conversationId, message.key, message.senderId, Constant.MESSAGE_STATUS_DELIVERED)
                                        .flatMap {
                                            getConversationValueUseCase.buildUseCaseObservable(message.conversationId)
                                                    .flatMap { conversation ->
                                                        sendMessageNotificationUseCase.buildUseCaseObservable(SendMessageNotificationUseCase.Params(conversation, message.key, "${message.childMessages.size}", MessageType.IMAGE_GROUP))
                                                    }
                                        }
                            }
                }
    }

    private fun resendImageMessages(messages: List<MessageEntity>): Observable<Boolean> {
        if (messages.isEmpty()) return Observable.just(true)
        return Observable.just(messages)
                .flatMapIterable { list -> list }
                .flatMap { message ->
                    val localFile = if (message.messageType == Constant.MSG_TYPE_IMAGE) File(message.photoUrl) else File(message.gameUrl)
                    if (!localFile.exists()) return@flatMap Observable.just(true)
                    uploadThumbnail(message.conversationId, localFile.absolutePath)
                            .concatMap { thumbUrl ->
                                uploadImage(message.conversationId, localFile.absolutePath)
                                        .flatMap { photoUrl ->
                                            val updateValue = HashMap<String, Any>()
                                            if (message.messageType == Constant.MSG_TYPE_IMAGE) {
                                                updateValue[String.format("messages/%s/%s/photoUrl", message.conversationId, message.key)] = photoUrl
                                                updateValue[String.format("media/%s/%s/photoUrl", message.conversationId, message.key)] = photoUrl
                                            } else {
                                                updateValue[String.format("messages/%s/%s/gameUrl", message.conversationId, message.key)] = photoUrl
                                                updateValue[String.format("media/%s/%s/gameUrl", message.conversationId, message.key)] = photoUrl
                                            }
                                            updateValue[String.format("messages/%s/%s/updateAt", message.conversationId, message.key)] = System.currentTimeMillis()/1000.0
                                            updateValue[String.format("messages/%s/%s/thumbUrl", message.conversationId, message.key)] = thumbUrl
                                            updateValue[String.format("media/%s/%s/thumbUrl", message.conversationId, message.key)] = thumbUrl
                                            updateValue["messages/${message.conversationId}/${message.key}/status/${message.senderId}"] = Constant.MESSAGE_STATUS_DELIVERED
                                            commonRepository.updateBatchData(updateValue)
                                                    .flatMap {
                                                        messageRepository.updateLocalMessageStatus(message.key, Constant.MESSAGE_STATUS_DELIVERED)
                                                        getConversationValueUseCase.buildUseCaseObservable(message.conversationId)
                                                                .flatMap { conversation ->
                                                                    sendMessageNotificationUseCase.buildUseCaseObservable(SendMessageNotificationUseCase.Params(conversation, message.key, "", MessageType.from(message.messageType)))
                                                                }
                                                    }
                                        }
                            }
                }
    }

    private fun uploadImage(conversationKey: String, filePath: String): Observable<String> {
        if (TextUtils.isEmpty(filePath)) return Observable.just("")
        val fileName = File(filePath).name
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 512, 512))
    }

    private fun uploadThumbnail(conversationKey: String, filePath: String): Observable<String> {
        if (TextUtils.isEmpty(filePath)) return Observable.just("")
        val fileName = "thumb_" + File(filePath).name
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 128, 128))
    }

    private fun resendFileMessages(messages: List<MessageEntity>): Observable<Boolean> {
        if (messages.isEmpty()) return Observable.just(true)
        return Observable.just(messages)
                .flatMapIterable { list -> list }
                .flatMap { message ->
                    val localFile = if (message.messageType == Constant.MSG_TYPE_VOICE) File(message.audioUrl) else File(message.videoUrl)
                    if (!localFile.exists()) return@flatMap Observable.just(true)
                    uploadFile(message.conversationId, localFile.absolutePath)
                            .flatMap { audioUrl ->
                                val updateValue = HashMap<String, Any>()
                                if (message.messageType == Constant.MSG_TYPE_VOICE) {
                                    updateValue["messages/${message.conversationId}/${message.key}/audioUrl"] = audioUrl
                                } else if (message.messageType == Constant.MSG_TYPE_VIDEO) {
                                    updateValue["messages/${message.conversationId}/${message.key}/videoUrl"] = audioUrl
                                }
                                updateValue["messages/${message.conversationId}/${message.key}/status/${message.senderId}"] = Constant.MESSAGE_STATUS_DELIVERED
                                updateValue["messages/${message.conversationId}/${message.key}/updateAt"] = System.currentTimeMillis()/1000.0

                                commonRepository.updateBatchData(updateValue)
                                        .flatMap {
                                            messageRepository.updateLocalMessageStatus(message.key, Constant.MESSAGE_STATUS_DELIVERED)
                                            getConversationValueUseCase.buildUseCaseObservable(message.conversationId)
                                                    .flatMap { conversation ->
                                                        sendMessageNotificationUseCase.buildUseCaseObservable(
                                                                SendMessageNotificationUseCase.Params(conversation, message.key, "", MessageType.from(message.messageType)))
                                                    }
                                        }
                            }
                }
    }

    private fun uploadFile(conversationKey: String, filePath: String): Observable<String> {
        return if (TextUtils.isEmpty(filePath)) Observable.just("") else storageRepository.uploadFile(conversationKey, filePath)
    }
}