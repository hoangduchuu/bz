package com.ping.android.presentation.view.flexibleitem.messages

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.utils.CommonMethod
import com.ping.android.utils.UiUtils
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.io.File

abstract class VideoMessageBaseItem(message: Message) : MessageBaseItem<VideoMessageBaseItem.ViewHolder>(message) {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder
            = VideoMessageBaseItem.ViewHolder(parent.inflate(layoutId, false))

    class ViewHolder(itemView: View): MessageBaseItem.ViewHolder(itemView) {
        private var videoThumbnail: ImageView = itemView.findViewById(R.id.video_thumbnail)
        private var container: RelativeLayout = itemView.findViewById(R.id.container)
        private var isVideoReady = false
        private var videoFile: File? = null

        init {
            initGestureListener()
        }

        override fun bindData(item: MessageBaseItem<*>?, lastItem: Boolean) {
            isVideoReady = false
            super.bindData(item, lastItem)
            val videoItem = item as VideoMessageBaseItem
            val cacheVideo = File(getLocalFilePath(videoItem.message.videoUrl))
            if (cacheVideo.exists()) {
                // Prepare UI
                setupUI(cacheVideo)
            } else {
                // Download video file
                downloadVideoFile(videoItem.message.videoUrl, cacheVideo)
            }
        }

        private fun setupUI(videoFile: File) {
            if (!videoFile.exists()) return
            val thumbnail = UiUtils.retrieveVideoFrameFromVideo(itemView.context, videoFile.absolutePath)
            if (thumbnail != null) {
                GlideApp.with(itemView)
                        .load(thumbnail)
                        .placeholder(videoThumbnail.drawable)
                        .transform(RoundedCornersTransformation(20, 0))
                        .into(videoThumbnail)
                isVideoReady = true
                this.videoFile = videoFile
                //videoThumbnail.setImageBitmap(thumbnail)
            }
        }

        private fun downloadVideoFile(videoUrl: String, file: File) {
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl)
            storageReference.getFile(file).addOnSuccessListener {
                // Download success
                setupUI(file)
            }
        }

        override fun onSingleTap() {
            if (!isVideoReady || videoFile == null) return

            messageListener?.openVideo(videoFile!!.absolutePath)
        }

        override fun getClickableView(): View? {
            return container
        }

        override fun getSlideView(): View {
            return container
        }

        private fun getLocalFilePath(videoUrl: String): String {
            val videoName = CommonMethod.getFileNameFromFirebase(videoUrl)
            return itemView.context
                    .externalCacheDir!!.absolutePath + File.separator + videoName
        }
    }
}