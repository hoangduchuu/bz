package com.ping.android.presentation.view.custom.facerecogloading

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.orhanobut.logger.Logger
import com.ping.android.R
import com.ping.android.utils.Log

/**
 * Created by Huu Hoang
 * This VIEW provides:
 *  - showSuccesss()
 *  - showError()
 *  - showLoading()
 *  to use in other land , where need to loading while recogize
 */
class LoadingRecognizeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    var flipperSuccess: com.wajahatkarim3.easyflipview.EasyFlipView
    var flipperFalied: com.wajahatkarim3.easyflipview.EasyFlipView

    init {
        LayoutInflater.from(context)
                .inflate(R.layout.view_loading_recognize, this, true)
        flipperSuccess = findViewById(R.id.flipperSuccess)
        flipperFalied = findViewById(R.id.flipperFalied)

        registerLisetner()

    }

    private fun registerLisetner() {
        flipperSuccess.setOnFlipListener { easyFlipView, newCurrentSide ->
            easyFlipView.flipTheView(true)
        }
    }


    fun showSuccess() {
        flipperSuccess.visibility = View.VISIBLE
        flipperFalied.visibility = View.GONE
        flipperSuccess.flipTheView()
    }

    fun showLoading() {
        flipperSuccess.visibility = View.VISIBLE
        flipperFalied.visibility = View.GONE
    }

    fun showError() {
        flipperFalied.visibility = View.VISIBLE
        flipperSuccess.visibility = View.GONE
        flipperFalied.flipTheView()
    }

    fun nextLoading(){
        flipperFalied.visibility = View.GONE
        flipperSuccess.visibility = View.GONE

        flipperSuccess.visibility = View.VISIBLE
        flipperFalied.visibility = View.GONE
    }
}