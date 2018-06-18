package com.ping.android.presentation.view.flexibleitem.messages

import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.bzzzchat.extensions.px
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.utils.CommonMethod
import com.ping.android.utils.UiUtils
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import org.w3c.dom.Text
import java.io.File

abstract class VideoMessageBaseItem(message: Message) : MessageBaseItem<VideoMessageBaseItem.ViewHolder>(message) {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder
            = VideoMessageBaseItem.ViewHolder(parent.inflate(layoutId, false))

    class ViewHolder(itemView: View): MessageBaseItem.ViewHolder(itemView) {
        private var videoThumbnail: ImageView = itemView.findViewById(R.id.video_thumbnail)
        private var container: RelativeLayout = itemView.findViewById(R.id.container)
        private var imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)
        private var isVideoReady = false
        private var videoFile: File? = null

        init {
            initGestureListener()
        }

        override fun bindData(item: MessageBaseItem<*>?, lastItem: Boolean) {
            isVideoReady = false
            super.bindData(item, lastItem)
            imgPlay.visibility = View.GONE
            val videoItem = item as VideoMessageBaseItem
            val cacheVideo = File(getLocalFilePath(videoItem.message))
            if (cacheVideo.exists()) {
                // Prepare UI
                setupUI(cacheVideo)
            } else {
                if (TextUtils.isEmpty(videoItem.message.videoUrl)) {
                    imgPlay.visibility = View.VISIBLE
                    imgPlay.setImageResource(R.drawable.ic_error_outline)
                    imgPlay.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
                    return
                }
                // Download video file
                downloadVideoFile(videoItem.message.videoUrl, cacheVideo)
            }
        }

        private fun setupUI(videoFile: File) {
            if (!videoFile.exists() || itemView == null || itemView.context == null) return
            imgPlay.visibility = View.VISIBLE
            imgPlay.setImageResource(R.drawable.ic_play_arrow)
            imgPlay.background = ContextCompat.getDrawable(itemView.context, R.drawable.background_circle_gray_dark)
            val thumbnail = UiUtils.retrieveVideoFrameFromVideo(itemView.context, videoFile.absolutePath)
            val radius = 30.px
            if (thumbnail != null) {
                GlideApp.with(itemView)
                        .load(thumbnail)
                        .placeholder(videoThumbnail.drawable)
                        .transform(RoundedCornersTransformation(radius, 0))
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
            storageReference.getFile(file)
                    .addOnSuccessListener {
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

        private fun getLocalFilePath(message: Message): String {
            if (!TextUtils.isEmpty(message.localFilePath)) {
                return message.localFilePath
            }
            val videoName = CommonMethod.getFileNameFromFirebase(message.videoUrl)
            return itemView.context
                    .externalCacheDir!!.absolutePath + File.separator + videoName
        }
    }
}