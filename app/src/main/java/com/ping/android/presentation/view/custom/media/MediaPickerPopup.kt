package com.ping.android.presentation.view.custom.media

import android.app.Activity
import android.view.ViewTreeObserver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import com.bzzzchat.videorecorder.view.withDelay
import com.ping.android.utils.ResourceUtils.dpToPx
import com.ping.android.utils.Utils

class MediaPickerPopup(
        val context: Activity,
        private val rootView: View,
        private val editText: EditText,
        var dismissListener: (() -> Unit)? = null
) {
    private val MIN_KEYBOARD_HEIGHT = 100
    private lateinit var popupWindow: PopupWindow
    private var isKeyboardOpen = false
    private var isPendingOpen = false
    private var mediaPickerView: MediaPickerView = MediaPickerView(context)

    private val onGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val rect = Utils.windowVisibleDisplayFrame(context)
            val heightDifference = Utils.screenHeight(context) - rect.bottom

            if (heightDifference > dpToPx(MIN_KEYBOARD_HEIGHT)) {
                popupWindow.height = heightDifference
                popupWindow.width = rect.right
                isKeyboardOpen = true

                if (isPendingOpen) {
                    showAtBottom()
                    isPendingOpen = false
                }
            } else {
                if (isKeyboardOpen) {
                    isKeyboardOpen = false

                    dismiss()
                    context.window.decorView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        }
    }

    init {
        mediaPickerView.initProvider(context)
        mediaPickerView.refreshData()
        popupWindow = PopupWindow(context)
        popupWindow.contentView = mediaPickerView
        popupWindow.inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        popupWindow.setBackgroundDrawable(BitmapDrawable(context.resources, null as Bitmap?)) // To avoid borders and overdraw.
        popupWindow.setOnDismissListener {
            dismissListener?.invoke()
        }
    }

    fun setListener(listener: MediaPickerListener) {
        mediaPickerView.listener = listener
    }

    fun toggle() {
        if (!popupWindow.isShowing) {
            // Remove any previous listeners to avoid duplicates.
            context.window.decorView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
            context.window.decorView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)

            if (isKeyboardOpen) {
                // If the keyboard is visible, simply show the emoji popup.
                showAtBottom()
            } else {
                // Open the text keyboard first and immediately after that show the emoji popup.
                editText.isFocusableInTouchMode = true
                editText.requestFocus()

                showAtBottomPending()

                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            dismiss()
        }

        // Manually dispatch the event. In some cases this does not work out of the box reliably.
        context.window.decorView.viewTreeObserver.dispatchOnGlobalLayout()
    }

    fun isShowing(): Boolean {
        return popupWindow.isShowing
    }

    private fun showAtBottom() {
        val desiredLocation = Point(0, Utils.screenHeight(context) - popupWindow.height)

        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, desiredLocation.x, desiredLocation.y)
        Utils.fixPopupLocation(popupWindow, desiredLocation)
    }

    private fun showAtBottomPending() {
        if (isKeyboardOpen) {
            showAtBottom()
        } else {
            isPendingOpen = true
        }
    }

    fun dismiss() {
        popupWindow.dismiss()
    }
}