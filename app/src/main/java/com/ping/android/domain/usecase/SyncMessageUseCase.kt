package com.ping.android.domain.usecase

import android.text.TextUtils
import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.data.entity.MessageEntity
import com.ping.android.domain.repository.CommonRepository
import com.ping.android.domain.repository.MessageRepository
import com.ping.android.domain.repository.StorageRepository
import com.ping.android.model.Message
import com.ping.android.utils.Utils
import com.ping.android.utils.configs.Constant
import io.reactivex.Observable
import java.io.File
import javax.inject.Inject

class SyncMessageUseCase @Inject constructor(
        threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : UseCase<Boolean, Void?>(threadExecutor, postExecutionThread) {

    @Inject
    lateinit var commonRepository: CommonRepository
    @Inject
    lateinit var messageRepository: MessageRepository
    @Inject
    lateinit var storageRepository: StorageRepository

    override fun buildUseCaseObservable(params: Void?): Observable<Boolean> {
        return commonRepository.observeConnectionState()
                .flatMap { isConnected ->
                    return@flatMap if (isConnected) {
                        messageRepository.errorMessages
                                .flatMap { messages ->
                                    val imageMessages = messages.filter {
                                        it.messageType in arrayOf(Constant.MSG_TYPE_IMAGE, Constant.MSG_TYPE_GAME)
                                    }
                                    val textMessages = messages.filter {
                                        it.messageType == Constant.MSG_TYPE_TEXT
                                    }
                                    return@flatMap resendTextMessage(textMessages)
                                            .concatMap { resendImageMessages(ArrayList(imageMessages)) }
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
        }
        return commonRepository.updateBatchData(updateValue)
    }

    private fun resendImageMessages(messages: List<MessageEntity>): Observable<Boolean> {
        if (messages.isEmpty()) return Observable.just(true)
        return Observable.just(messages)
                .flatMapIterable { list -> list }
                .flatMap { message ->
                    val localFile = if (message.messageType == Constant.MSG_TYPE_IMAGE) File(message.photoUrl) else File(message.gameUrl)
                    if (!localFile.exists()) return@flatMap Observable.just(true)
                    return@flatMap uploadThumbnail(message.conversationId, message.key, localFile.absolutePath)
                            .concatMap { thumbUrl ->
                                uploadImage(message.conversationId, message.key, localFile.absolutePath)
                                        .flatMap { photoUrl ->
                                            val updateValue = java.util.HashMap<String, Any>()
                                            if (message.messageType == Constant.MSG_TYPE_IMAGE) {
                                                updateValue[String.format("messages/%s/%s/photoUrl", message.conversationId, message.key)] = photoUrl
                                                updateValue[String.format("media/%s/%s/photoUrl", message.conversationId, message.key)] = photoUrl
                                            } else {
                                                updateValue[String.format("messages/%s/%s/gameUrl", message.conversationId, message.key)] = photoUrl
                                                updateValue[String.format("media/%s/%s/gameUrl", message.conversationId, message.key)] = photoUrl
                                            }
                                            updateValue[String.format("messages/%s/%s/thumbUrl", message.conversationId, message.key)] = thumbUrl
                                            updateValue[String.format("media/%s/%s/thumbUrl", message.conversationId, message.key)] = thumbUrl
                                            updateValue["messages/${message.conversationId}/${message.key}/status/${message.senderId}"] = Constant.MESSAGE_STATUS_DELIVERED
                                            return@flatMap commonRepository.updateBatchData(updateValue)
                                        }
                            }
                }

    }

    private fun uploadImage(conversationKey: String, messageKey: String, filePath: String): Observable<String> {
        if (TextUtils.isEmpty(filePath)) return Observable.just("")
        val fileName = File(filePath).name
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 512, 512))
                .flatMap { s -> messageRepository.updateImage(conversationKey, messageKey, s) }
    }

    private fun uploadThumbnail(conversationKey: String, messageKey: String, filePath: String): Observable<String> {
        if (TextUtils.isEmpty(filePath)) return Observable.just("")
        val fileName = "thumb_" + File(filePath).name
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 128, 128))
                .flatMap { s -> messageRepository.updateThumbnailImage(conversationKey, messageKey, s) }
    }
}