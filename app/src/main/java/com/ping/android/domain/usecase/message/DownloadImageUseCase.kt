package com.ping.android.domain.usecase.message

import com.bzzzchat.cleanarchitecture.PostExecutionThread
import com.bzzzchat.cleanarchitecture.ThreadExecutor
import com.bzzzchat.cleanarchitecture.UseCase
import com.ping.android.device.Device
import com.ping.android.domain.repository.StorageRepository
import com.ping.android.model.Message
import com.ping.android.utils.UiUtils
import io.reactivex.Observable
import java.io.File
import javax.inject.Inject

class DownloadImageUseCase @Inject constructor(
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread
) : UseCase<Boolean, Message>(threadExecutor, postExecutionThread) {
    @Inject lateinit var storageRepository: StorageRepository
    @Inject lateinit var device: Device

    override fun buildUseCaseObservable(params: Message): Observable<Boolean> {
        var name = UiUtils.getFileName(params.mediaUrl)
        val extension = name.split(".").last()
        val validExtensions = arrayListOf("jpg", "jpeg", "png")
        if (!validExtensions.contains(extension)) {
            name += ".jpg"
        }
        val fileToSave = File(device.externalImageFolder, name)
        return storageRepository.downloadFile(params.mediaUrl, fileToSave.absolutePath)
                .doOnNext {success -> device.refreshMedia(fileToSave.absolutePath) }
    }
}