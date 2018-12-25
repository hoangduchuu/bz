package com.ping.android.presentation.view.custom.facerecogloading

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.ping.android.R
import com.wajahatkarim3.easyflipview.EasyFlipView

/**
 * Created by Huu Hoang
 * This VIEW provides:
 *  - showSuccess()
 *  - showError()
 *  - showLoading()
 *  to use in other land , where need to loading while recogize
 */
class FaceRecognizeIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var flipperSuccess: com.wajahatkarim3.easyflipview.EasyFlipView
    private var flipperFail: com.wajahatkarim3.easyflipview.EasyFlipView

    init {
        LayoutInflater.from(context)
                .inflate(R.layout.view_loading_recognize, this, true)
        flipperSuccess = findViewById(R.id.flipperSuccess)
        flipperFail = findViewById(R.id.flipperFail)

        registerListener()

    }

    private fun registerListener() {
        flipperSuccess.setOnFlipListener { easyFlipView, newCurrentSide ->
            easyFlipView.flipTheView(true)
        }
    }


    fun showSuccess() {
        flipperSuccess.visibility = View.VISIBLE
        flipperFail.visibility = View.GONE
        flipperSuccess.flipTheView()
    }

    fun showLoading() {
        if (flipperSuccess.currentFlipState == EasyFlipView.FlipState.BACK_SIDE){
            flipperSuccess.flipTheView()
        }
        flipperSuccess.visibility = View.VISIBLE
        flipperFail.visibility = View.GONE
    }

    fun showError() {
        flipperFail.visibility = View.VISIBLE
        flipperSuccess.visibility = View.GONE
        flipperFail.flipTheView()
    }

    fun nextLoading(){
        flipperFail.visibility = View.GONE
        flipperSuccess.visibility = View.GONE

        flipperSuccess.visibility = View.VISIBLE
        flipperFail.visibility = View.GONE
    }
}