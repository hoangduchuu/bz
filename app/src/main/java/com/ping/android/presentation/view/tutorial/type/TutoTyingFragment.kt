package com.ping.android.presentation.view.tutorial.type;

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.ping.android.R
import com.ping.android.presentation.view.custom.EmojiGifEditText
import com.ping.android.presentation.view.fragment.BaseFragment
import com.ping.android.presentation.view.tutorial.utils.EmojConverter
import com.ping.android.utils.BzzzLog
import com.ping.android.utils.KeyboardHelpers
import com.ping.android.utils.Log
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.view_tutorial_input_chat_bottom_typing.*
import javax.inject.Inject

/**
 * Created by Huu Hoang on 27/12/2018
 */
class TutoTyingFragment : BaseFragment(), TutoTypingContract.View {

    //region variable region
    var isSelected = false;

    private var textWatcher: TextWatcher? = null

    private var originalText = ""

    private var messageBeforeChange = ""

    private var selectPosition = 0

    @Inject
    lateinit var userManager: EmojConverter

    private lateinit var edMessage: EmojiGifEditText






    // endregion

    //region Android life cycle region

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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

        edMessage = edt_emoji_msg

        originalText = context?.resources?.getString(R.string.content_demo_messages)!!


        tgMarkStatus.isSelected = false
        updateMaskTintColor(tgMarkStatus.isSelected)
        edt_emoji_msg.setOnClickListener {
            KeyboardHelpers.showKeyboard(context, edt_emoji_msg)
        }
        // when click btn mark
        tgMarkStatus.setOnClickListener {
            onChangeTypingMark()
        }

        initTextWatcher()

    }


    private fun onChangeTypingMark() {
        tgMarkStatus.isSelected = !tgMarkStatus.isSelected

        edMessage.removeTextChangedListener(textWatcher)
        var select = edMessage.selectionStart
        if (tgMarkStatus.isSelected) {
            updateMaskTintColor(true)
            edMessage.setText(userManager.encodeMessage(originalText))
            select = userManager.encodeMessage(originalText.substring(0, select))!!.length
        } else {
            updateMaskTintColor(false)
            //int color = ContextCompat.getColor(this, R.color.gray_color);
            edMessage.setText(originalText)
        }
        try {
            if (select > 0 && select <= edMessage.text!!.length) {
                edMessage.setSelection(select)
            } else {
                edMessage.setSelection(edMessage.text!!.length)

            }
        } catch (ex: IndexOutOfBoundsException) {
            Log.e(ex)
        }

        edMessage.addTextChangedListener(textWatcher)
    }

    private fun updateMaskTintColor(isEnable: Boolean) = if (!isEnable) {
       val color = ContextCompat.getColor(context!!, R.color.color_grey)
       tgMarkStatus.backgroundTintList = ColorStateList.valueOf(color)

   } else {
      val color = ContextCompat.getColor(context!!, R.color.black)
       tgMarkStatus.backgroundTintList = ColorStateList.valueOf(color)
   }



    @SuppressLint("ClickableViewAccessibility")
    private fun initTextWatcher() {
        if (textWatcher != null) return
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                messageBeforeChange = charSequence.toString()
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                if (!tgMarkStatus.isSelected) {
                    return
                }
                val isAppending = count > 0
                val isAppendingEnd = isAppending && (start + count == charSequence.length)
                val isAppendingStart = isAppending && start == 0

                if (isAppending) {
                    val newCharacter = charSequence.subSequence(start, start + count)
                    if (isAppendingEnd) {
                        originalText += newCharacter
                        val newMessageEncoded = userManager.encodeMessage(originalText)
                        selectPosition = newMessageEncoded.length
                    } else if (isAppendingStart) {
                        originalText = "$newCharacter$originalText"
                        val newCharacterEncoded = userManager.encodeMessage(newCharacter.toString())
                        selectPosition = newCharacterEncoded.length - 1
                    } else {
                        val newSubSequence = charSequence.dropLast(charSequence.length - start).toString()
                        val stringBuffer = StringBuffer()
                        val size = originalText.toCharArray().size
                        for (i in 0 until size) {
                            val cha = originalText[i]
                            stringBuffer.append(userManager.encodeMessage(cha.toString()))
                            if (!newSubSequence.contains(stringBuffer.toString(), false)) {
                                originalText = originalText.substring(0, i) + newCharacter + originalText.substring(i, originalText.length)
                                break
                            } else {
                                selectPosition = stringBuffer.length
                            }
                        }
                    }
                } else {
                    // Deleting
                    val newSubSequence = charSequence.dropLast(charSequence.length - start).toString()
                    val stringBuffer = StringBuffer()
                    val size = originalText.toCharArray().size
                    for (i in 0 until size) {
                        val cha = originalText[i]
                        stringBuffer.append(userManager.encodeMessage(cha.toString()))
                        if (!newSubSequence.contains(stringBuffer.toString(), false)) {
                            originalText = originalText.substring(0, i) + originalText.substring(i + 1, originalText.length)
                            break
                        } else {
                            selectPosition = stringBuffer.length
                        }
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {


                if (!tgMarkStatus.isSelected) {
                    originalText = editable.toString()
                    return
                }

                val encodeText = userManager.encodeMessage(originalText)
                if (TextUtils.equals(edMessage.text, encodeText)) {
                    return
                }

                edMessage.removeTextChangedListener(textWatcher)
                edMessage.setText(encodeText)
                BzzzLog.d(encodeText)
                if (selectPosition > 0 && selectPosition + 1 <= encodeText!!.length) {
                    edMessage.setSelection(selectPosition + 1)
                } else {
                    edMessage.setSelection(encodeText!!.length)
                }
                edMessage.addTextChangedListener(textWatcher)
            }
        }
        edMessage.addTextChangedListener(textWatcher)
        edMessage.setOnTouchListener { view, motionEvent ->
            KeyboardHelpers.showKeyboard(context, edMessage)
            false
        }
        edMessage.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {

                KeyboardHelpers.showKeyboard(context, edMessage)
            }
        }
    }



    // endregion

}