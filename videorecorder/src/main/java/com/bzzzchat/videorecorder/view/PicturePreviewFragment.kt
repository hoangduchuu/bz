package com.bzzzchat.videorecorder.view

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.signature.ObjectKey
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.videorecorder.R

import kotlinx.android.synthetic.main.fragment_img_preview.*

class PicturePreviewFragment: Fragment() {
    private lateinit var imageFile: String

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater!!.inflate(R.layout.fragment_img_preview, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnBack.setOnClickListener { activity.onBackPressed() }
        arguments.let {
            imageFile = it.getString("imgPath")
        }
        GlideApp.with(this)
                .load(imageFile)
                .signature(ObjectKey(System.currentTimeMillis()))
                .into(imgPreview)
    }

    companion object {
        @JvmStatic
        fun newInstance(extras: Bundle) = PicturePreviewFragment().apply {
            arguments = extras
        }
    }
}