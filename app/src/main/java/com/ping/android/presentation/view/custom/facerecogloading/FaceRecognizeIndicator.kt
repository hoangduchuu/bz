package com.ping.android.presentation.view.custom.facerecogloading

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
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
    private val animFadeIn = AnimationUtils.loadAnimation(context,
            R.anim.zoom_in_loop)
    private var isAnimatorAdded = false
    init {
        LayoutInflater.from(context)
                .inflate(R.layout.view_loading_recognize, this, true)
        flipperSuccess = findViewById(R.id.flipperSuccess)
        flipperFail = findViewById(R.id.flipperFail)

    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            clearAnimation()
            isAnimatorAdded = false
        }else if (!isAnimatorAdded && visibility == View.VISIBLE){
            startAnimation(animFadeIn)
            isAnimatorAdded = true
        }
    }


    fun showSuccess() {
        flipperSuccess.visibility = View.VISIBLE
        flipperFail.visibility = View.GONE
        flipperSuccess.flipTheView(true)
        val handler = Handler()
        handler.postDelayed({
            visibility = View.GONE
            flipperSuccess.visibility = View.GONE
            flipperSuccess.flipTheView(true)
        }, 1000)
    }

    fun showLoading() {
        visibility = View.GONE
        flipperSuccess.visibility = View.VISIBLE
        flipperFail.visibility = View.GONE
        visibility = View.VISIBLE
    }

    fun showError() {
        flipperFail.visibility = View.VISIBLE
        flipperSuccess.visibility = View.GONE
        flipperFail.flipTheView(true)
        val handler = Handler()
        handler.postDelayed({
            flipperFail.visibility = View.GONE
            flipperSuccess.visibility = View.GONE

            flipperSuccess.visibility = View.VISIBLE
            flipperFail.visibility = View.GONE
        }, 1000)
    }
}