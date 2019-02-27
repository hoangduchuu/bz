package com.ping.android.presentation.view.flexibleitem.messages

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
        private lateinit var loadingView:View
        private  var width : Int? = null


        init {
            initGestureListener()
            width = getFullWidth()
        }

        override fun bindData(item: MessageBaseItem<*>?, lastItem: Boolean) {
            isVideoReady = false
            super.bindData(item, lastItem)
            imgPlay.visibility = View.GONE
            loadingView = itemView.findViewById(R.id.loading_container)
            loadingView.visibility = View.VISIBLE

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
            scaleLoadingViewHolder()
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
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                return true
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                val h = ( resource as BitmapDrawable).bitmap.height;
                                val w = resource.bitmap.width;
                                calculateImageViewSize(w.toFloat(),h.toFloat(),width!!.toFloat())
                                return false
                            }
                        })
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
                                    .listener(object : RequestListener<Drawable> {
                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                            return true
                                        }

                                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                            val h = ( resource as BitmapDrawable).bitmap.height;
                                            val w = resource.bitmap.width;
                                            calculateImageViewSize(w.toFloat(),h.toFloat(),width!!.toFloat())
                                            return false
                                        }
                                    })
                                    .into(videoThumbnail)
                        }
            }
            isVideoReady = true
            loadingView.visibility = View.GONE

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
            if (faceIdStatusRepository.isFaceIdEnabled() && !faceIdStatusRepository.faceIdRecognitionStatus.get() ) return
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


        /**
         * get Width of device
         */
        private fun getFullWidth(): Int {
            val displayMetrics = DisplayMetrics()
            (itemView.context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            return displayMetrics.widthPixels
        }


        /**
         * calculate ImageView size Based-on image ratio.
         */
        private fun calculateImageViewSize(w: Float, h: Float, parentWidth: Float) {
            if (w > h) {
                val imageViewWidth = (70 * parentWidth / 100).toInt()
                val imageViewHeight: Int = (imageViewWidth * (w / h)).toInt()
                val params = videoThumbnail.layoutParams;
                params?.width = imageViewWidth
                params?.height = imageViewHeight
                videoThumbnail.layoutParams = params
            } else {
                val imageViewHeight = (70 * parentWidth / 100).toInt()
                val imageViewWidth: Int = (imageViewHeight * (w / h)).toInt()
                val params = videoThumbnail.layoutParams;
                params?.width = imageViewWidth
                params?.height = imageViewHeight
                videoThumbnail.layoutParams = params
            }
        }


        /**
         * Scale ImageView Holder
         */
        private fun scaleLoadingViewHolder() {
            val holderHeight = 70 * width!! / 100
            val holderWidth = 4 * holderHeight / 6
            val params = loadingView.layoutParams
            params.height = holderHeight
            params.width = holderWidth
            loadingView.layoutParams = params


            val paramsImgPlay = videoThumbnail.layoutParams
            paramsImgPlay.height = holderHeight
            paramsImgPlay.width = holderWidth
            videoThumbnail.layoutParams = paramsImgPlay
        }
    }
}