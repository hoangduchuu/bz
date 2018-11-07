package com.ping.android.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

object BzzzViewUtils {

    /**
     * give a list of Editext,
     * this function help we remove the underline if editext is selected, re-underline if un-selected
     */
    fun removeUnderLineEditTextIfSelected(editTexts: List<EditText>) {
        val drawable = editTexts[0].background

//        for (i in editTexts.indices) {
//            editTexts[i].onFocusChangeListener = View.OnFocusChangeListener { v, forcusing ->
//                if (forcusing) {
//                    setBackgroundWithAnimation(editTexts[i], null)
//
//                } else {
//                    setBackgroundWithAnimation(editTexts[i], drawable)
//                }
//            }
//        }

    }
    fun removeUnderLineAndEyeIconEditTextIfSelected(parentEditexts: List<com.google.android.material.textfield.TextInputLayout>, editTexts: List<EditText>) {
        val drawable = editTexts[0].background

        for (i in editTexts.indices) {
            editTexts[i].onFocusChangeListener = View.OnFocusChangeListener { v, forcusing ->
                when {
                    forcusing -> {
                        setBackgroundWithAnimation(editTexts[i], null)
                        parentEditexts[i].isPasswordVisibilityToggleEnabled =true
                    }
                    else -> {
                        setBackgroundWithAnimation(editTexts[i], drawable)
                        parentEditexts[i].isPasswordVisibilityToggleEnabled =false

                    }
                }
            }
        }

    }

    private fun setBackgroundWithAnimation(edt: EditText, drawable: Drawable?) {
        val alphaAnimator = ObjectAnimator.ofFloat(edt, View.TRANSLATION_X, 0.0f, 1.0f)
        alphaAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                edt.background = drawable
            }
        })
        alphaAnimator.duration = 1000
        alphaAnimator.start()
    }


    /**
     * input TextInputLayout and child EditText
     * this function will show EYEBALL when EditText is selected, hide if un-selected
     */
    fun showEyeBallWhenTextbox(parentEditexts: List<com.google.android.material.textfield.TextInputLayout>, editTexts: List<EditText>) {
        val drawable = editTexts[0].background

        for (i in editTexts.indices) {
            editTexts[i].onFocusChangeListener = View.OnFocusChangeListener { v, forcusing ->
                when {
                    forcusing -> {
                        parentEditexts[i].isPasswordVisibilityToggleEnabled =true
                    }
                    else -> {
                        parentEditexts[i].isPasswordVisibilityToggleEnabled =false

                    }
                }
            }
        }
    }

}
