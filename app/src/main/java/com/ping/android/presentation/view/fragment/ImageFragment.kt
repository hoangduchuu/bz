package com.ping.android.presentation.view.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.utils.configs.Constant
import com.ping.android.utils.BitmapEncode
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.configuration.GlideRequest
import com.ping.android.App
import com.ping.android.presentation.view.activity.CoreActivity
import com.ping.android.presentation.view.custom.PullListener
import com.ping.android.utils.Log
import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.ImagePullEvent
import com.ping.android.utils.bus.events.ImageTapEvent
import kotlinx.android.synthetic.main.fragment_image.*
import java.io.File

/**
 * @author tuanluong
 */

class ImageFragment : Fragment() {
    private var messageKey: String = ""
    private lateinit var imageUrl: String
    private var isMask: Boolean = false
    private val background: ColorDrawable = ColorDrawable(Color.BLACK)
    private var translateY = 0.0f

    private var busProvider: BusProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? CoreActivity)?.let {
            busProvider = it.applicationComponent.provideBusProvider()
        }
        arguments?.let {
            messageKey = it.getString("messageKey")
            imageUrl = it.getString("imageUrl")
            isMask = it.getBoolean("isMask")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_image, container, false)
        view.background = background
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        image_detail.transitionName = messageKey
        image_detail.minimumScale = 0.8f
        image_detail.setOnPhotoTapListener { view, x, y ->
            busProvider?.post(ImageTapEvent())
        }
        puller.setListener(object: PullListener {
            override fun onPullStart() {
                busProvider?.post(ImagePullEvent(true))
            }

            override fun onPullProgress(diff: Float) {
                image_detail.translationY = diff
                if (Math.abs(diff) < 100) {
                    val scaleValue = Math.abs(diff) / 100 * (1.0f - image_detail.minimumScale)
                    image_detail.scale = 1 - scaleValue
                    updateBackgroundOpacity(Math.abs(diff / 8) / 100)
                } else {
                    image_detail.scale = image_detail.minimumScale
                    updateBackgroundOpacity(0.2f)
                }
            }

            override fun onPullEnd() {
                val translationY = image_detail.translationY
                if (Math.abs(translationY) > 90) {
                    activity?.onBackPressed()
                } else {
                    image_detail.translationY = 0.0f
                    image_detail.scale = 1.0f
                    updateBackgroundOpacity(0f)
                    busProvider?.post(ImagePullEvent(false))
                }
            }
        })
        bindData()
    }

    fun updateData(message: Message) {
        bindData()
    }

    private fun updateBackgroundOpacity(progress: Float) {
        val finalProgress = Math.min(1f, progress * 3f)
        Log.d("Update background opacity $finalProgress")
        background.alpha = (0xff * (1f - finalProgress)).toInt()
    }

    fun bindData() {
        val url: String = this.imageUrl
        if (TextUtils.isEmpty(url)) {
            return
        }
        val listener = object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                parentFragment?.startPostponedEnterTransition()
                activity?.startPostponedEnterTransition()
                return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                parentFragment?.startPostponedEnterTransition()
                activity?.startPostponedEnterTransition()
                return false
            }
        }
        val request: GlideRequest<Drawable>
        request = if (!url.startsWith("gs://")) {
            GlideApp.with(this)
                    .load(File(url))
        } else {
            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
            GlideApp.with(this)
                    .load(gsReference)
        }

        request.override(512)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .transform(BitmapEncode(isMask))
                .signature(ObjectKey(String.format("%s%s", messageKey, if (isMask) "encoded" else "decoded")))
                .listener(listener)
                .into(image_detail)
    }

    companion object {
        @JvmStatic
        fun newInstance(message: Message) =
                ImageFragment().apply {
                    val url: String = when (message.messageType) {
                        Constant.MSG_TYPE_IMAGE -> message.photoUrl
                        else -> message.gameUrl
                    }
                    arguments = Bundle().apply {
                        putString("messageKey", message.key)
                        putString("imageUrl", url)
                        putBoolean("isMask", message.isMask)
                    }
                }
    }
}

interface ImageListener {
    fun onImageTap()
}
