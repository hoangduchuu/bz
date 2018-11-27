package com.bzzzchat.videorecorder.view

import android.app.Fragment
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.videorecorder.R
import java.io.ByteArrayOutputStream


/**
 * show the captured picture
 */
/**
 * the listener
 */
interface ShowFaceFragmentListener{
    fun onBackToReCaptureButtonClicked()
    fun onFragmentOpening();
}
class ShowFaceFragment : Fragment() {
    private var listeners: ShowFaceFragmentListener? = null
    lateinit var byte: ByteArray
    lateinit var iv: ImageView
    lateinit var btnBack:ImageView
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_face_data, container, false)
        iv = view.findViewById(R.id.ivFace)
        btnBack = view.findViewById(R.id.ic_back)

        if (arguments != null) byte = arguments.getByteArray("path")
        val bmp = BitmapFactory.decodeByteArray(byte, 0, byte.size) // get bundle

        GlideApp.with(this).load(bmp).into(iv) // show img

        listeners?.onFragmentOpening()

        btnBack.setOnClickListener {
            listeners?.onBackToReCaptureButtonClicked()
        }

        return view // return to draw

    }

    /**
     * use this function instead create new Fragment in another activities
     */
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

    fun setListener(callBack: ShowFaceFragmentListener){
        this.listeners = callBack
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.listeners = context as? ShowFaceFragmentListener
    }


}