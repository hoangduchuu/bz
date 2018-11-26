package com.bzzzchat.videorecorder.view

import android.app.Fragment
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.videorecorder.R
import java.io.ByteArrayOutputStream


/**
 * show the captured picture
 */
class ShowFaceFragment : Fragment() {
    lateinit var byte: ByteArray
    lateinit var iv: ImageView
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_face_data, container, false)
        iv = view.findViewById(R.id.ivFace)
        if (arguments != null) byte = arguments.getByteArray("path")
        val bmp = BitmapFactory.decodeByteArray(byte, 0, byte.size)
        GlideApp.with(this).load(bmp).into(iv)
        return view

    }
    fun newInstance(path: Bitmap): ShowFaceFragment {
        val myFragment = ShowFaceFragment()
        val stream = ByteArrayOutputStream()
        path.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        val args = Bundle()
        args.putByteArray("path", byteArray)
        myFragment.arguments = args
        return myFragment
    }
}