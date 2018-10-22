package com.ping.android.presentation.view.flexibleitem.messages

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.bzzzchat.extensions.px
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.utils.CommonMethod
import com.ping.android.utils.UiUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.io.File

abstract class VideoMessageBaseItem(message: Message) : MessageBaseItem<VideoMessageBaseItem.ViewHolder>(message) {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = VideoMessageBaseItem.ViewHolder(parent.inflate(layoutId, false))

    class ViewHolder(itemView: View) : MessageBaseItem.ViewHolder(itemView) {
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
                if (TextUtils.isEmpty(videoItem.message.mediaUrl)) {
                    imgPlay.visibility = View.VISIBLE
                    imgPlay.setImageResource(R.drawable.ic_error_outline)
                    imgPlay.setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
                    return
                }
                // Download video file
                downloadVideoFile(videoItem.message.mediaUrl, cacheVideo)
            }
        }

        private fun setupUI(videoFile: File) {
            if (itemView.context is Activity) {
                val activity = itemView.context as Activity
                if (activity.isFinishing) return
            }
            if (!videoFile.exists() || itemView.context == null) return
            imgPlay.visibility = View.VISIBLE
            imgPlay.setImageResource(R.drawable.ic_play_arrow)
            imgPlay.background = ContextCompat.getDrawable(itemView.context, R.drawable.background_circle_gray_dark)
            val radius = 30.px
            val thumb = item.message.thumbUrl
            if (!TextUtils.isEmpty(thumb) && thumb.startsWith("gs://")) {
                val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(thumb)
                GlideApp.with(itemView)
                        .load(gsReference)
                        .placeholder(videoThumbnail.drawable)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(RoundedCornersTransformation(radius, 0))
                        .into(videoThumbnail)
            } else {
                val disposable = getLocalThumb(videoFile.absolutePath)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { thumbnail, error ->
                            GlideApp.with(itemView)
                                    .load(thumbnail)
                                    .placeholder(videoThumbnail.drawable)
                                    .transform(RoundedCornersTransformation(radius, 0))
                                    .into(videoThumbnail)
                        }
            }
            isVideoReady = true
            this.videoFile = videoFile
        }

        private fun getLocalThumb(videoPath: String): Single<Bitmap> {
            return Single.create { emitter ->
                val thumbnail = UiUtils.retrieveVideoFrameFromVideo(itemView.context, videoPath)
                if (thumbnail != null) {
                    emitter.onSuccess(thumbnail)
                }
            }
        }

        private fun downloadVideoFile(videoUrl: String, file: File) {
            if (TextUtils.isEmpty(videoUrl) || !videoUrl.startsWith("gs://")) {
                return
            }
            if (!file.exists() || !file.parentFile.exists()) {
                if (!CommonMethod.createFolder(file.parent)) {
                    return
                }
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
            var cacheVideoPath = message.localFilePath
            if (TextUtils.isEmpty(cacheVideoPath)) {
                if (!TextUtils.isEmpty(message.mediaUrl)) {
                    cacheVideoPath = if (message.mediaUrl.startsWith("gs://")) {
                        val videoName = CommonMethod.getFileNameFromFirebase(message.mediaUrl)
                        itemView.context
                                .externalCacheDir!!.absolutePath + File.separator + videoName
                    } else {
                        message.mediaUrl
                    }
                }
            }
            return cacheVideoPath
        }
    }
}