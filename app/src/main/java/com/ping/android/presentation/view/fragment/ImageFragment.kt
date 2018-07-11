package com.ping.android.presentation.view.fragment

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
import kotlinx.android.synthetic.main.fragment_image.*

class ImageFragment : Fragment() {
    private var messageKey: String = ""
    private lateinit var imageUrl: String
    private var isMask: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        image_detail.transitionName = messageKey
        bindData()
    }

    fun updateData(message: Message) {
        bindData()
    }

    fun bindData() {
        val url: String = this.imageUrl
        if (TextUtils.isEmpty(url) || !url.startsWith("gs://")) {
            return
        }
        val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
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
        GlideApp.with(this)
                .load(gsReference)
                .override(512)
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
