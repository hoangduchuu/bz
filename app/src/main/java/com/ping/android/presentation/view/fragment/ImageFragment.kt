package com.ping.android.presentation.view.fragment

import android.graphics.Bitmap
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
import com.ping.android.model.ImageMessage
import com.ping.android.model.Message
import com.ping.android.ultility.Constant
import com.ping.android.utils.BitmapEncode
import com.ping.android.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_image.*
import kotlinx.android.synthetic.main.item_gallery_image.view.*

class ImageFragment : Fragment() {
    private lateinit var message: Message

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            message = it!!.getParcelable("message")
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
        image_detail.transitionName = message.key
        bindData()
    }

    fun bindData() {
        val url: String = when (message.messageType) {
            Constant.MSG_TYPE_IMAGE -> message.photoUrl
            else -> message.gameUrl
        }
        if (TextUtils.isEmpty(url) || !url.startsWith("gs://")) {
            return
        }
        val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
        val listener = object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                parentFragment?.startPostponedEnterTransition()
                return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                parentFragment?.startPostponedEnterTransition()
                return false
            }
        }
        GlideApp.with(context)
                .load(gsReference)
                .override(500)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .transform(BitmapEncode(message.isMask))
                .signature(ObjectKey(String.format("%s%s", message.key, if (message.isMask) "encoded" else "decoded")))
                .listener(listener)
                .into(image_detail)
    }

    companion object {
        @JvmStatic
        fun newInstance(message: Message) =
                ImageFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("message", message)
                    }
                }
    }
}
