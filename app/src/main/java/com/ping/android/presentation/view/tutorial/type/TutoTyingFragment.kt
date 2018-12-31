package com.ping.android.presentation.view.tutorial.type;

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.ping.android.R
import com.ping.android.presentation.view.fragment.BaseFragment
import com.ping.android.utils.KeyboardHelpers
import com.ping.android.utils.Log
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.view_chat_bottom.*
import kotlinx.android.synthetic.main.view_tutorial_input_chat_bottom_typing.*

/**
 * Created by Huu Hoang on 27/12/2018
 */
class TutoTyingFragment : BaseFragment(), TutoTypingContract.View {

    //region variable region
    var isSelected = false;
    // endregion

    //region Android life cycle region

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tuto_typing, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpview()
    }


    // endregion


    //region override method region
    override fun showLoading() {
        super<BaseFragment>.showLoading()
    }

    override fun hideLoading() {
        super<BaseFragment>.hideLoading()
    }
    // endregion

    //region private method region
    private fun setUpview() {
        tgMarkStatus.isSelected = false
        updateMaskTintColor(tgMarkStatus.isSelected)
        edt_emoji_msg.setOnClickListener {
            KeyboardHelpers.showKeyboard(context, edt_emoji_msg)
        }
        // when click btn mark
        tgMarkStatus.setOnClickListener {
            tgMarkStatus.isSelected = !tgMarkStatus.isSelected
            updateMaskTintColor(tgMarkStatus.isSelected)
        }
    }

    private fun updateMaskTintColor(isEnable: Boolean) = if (!isEnable) {
        val color = ContextCompat.getColor(context!!, R.color.color_grey)
        tgMarkStatus.backgroundTintList = ColorStateList.valueOf(color)
        isSelected = true;
    } else {
        val color = ContextCompat.getColor(context!!, R.color.black)
        tgMarkStatus.backgroundTintList = ColorStateList.valueOf(color)
        isSelected = false;
    }

    // endregion

}