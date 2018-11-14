package com.ping.android.presentation.view.custom

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.PopupWindow
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.ping.android.R


/**
 * The keyboard height provider, this class uses a PopupWindow
 * to calculate the window height when the floating keyboard is opened and closed.
 */
class KeyboardHeightProvider
/**
 * Construct a new KeyboardHeightProvider
 *
 * @param activity The parent activity_main
 */
(
        /** The root activity_face_training that uses this KeyboardHeightProvider  */
        private val activity: AppCompatActivity) : PopupWindow(activity) {

    /** The keyboard height observer  */
    private var observer: KeyboardHeightObserver? = null

    /** The cached landscape height of the keyboard  */
    private var keyboardLandscapeHeight: Int = 0

    /** The cached portrait height of the keyboard  */
    private var keyboardPortraitHeight: Int = 0

    /** The view that is used to calculate the keyboard height  */
    private val popupView: View?

    /** The parent view  */
    private val parentView: View

    /**
     * Get the screen orientation
     *
     * @return the screen orientation
     */
    private val screenOrientation: Int
        get() = activity.resources.configuration.orientation

    init {

        val inflater = activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.popupView = inflater.inflate(R.layout.popupwindow, null, false)
        contentView = popupView

        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED

        parentView = activity.findViewById<View>(android.R.id.content)

        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT

        popupView!!.viewTreeObserver.addOnGlobalLayoutListener {
            if (popupView != null) {
                handleOnGlobalLayout()
            }
        }
    }

    /**
     * Start the KeyboardHeightProvider, this must be called after the onResume of the Activity.
     * PopupWindows are not allowed to be registered before the onResume has finished
     * of the Activity.
     */
    fun start() {
        if (!isShowing && parentView.windowToken != null) {
            setBackgroundDrawable(ColorDrawable(0))
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
        }
    }

    /**
     * Close the keyboard height provider,
     * this provider will not be used anymore.
     */
    fun close() {
        this.observer = null
        dismiss()
    }

    /**
     * Set the keyboard height observer to this provider. The
     * observer will be notified when the keyboard height has changed.
     * For example when the keyboard is opened or closed.
     *
     * @param observer The observer to be added to this provider.
     */
    fun setKeyboardHeightObserver(observer: KeyboardHeightObserver?) {
        this.observer = observer
    }

    private var lastKeyboardHeight: Int = 0

    /**
     * Popup window itself is as big as the window of the Activity.
     * The keyboard can then be calculated by extracting the popup view bottom
     * from the activity_face_training window height.
     */
    private fun handleOnGlobalLayout() {

        val screenSize = Point()
        activity.windowManager.defaultDisplay.getSize(screenSize)

        val rect = Rect()
        popupView!!.getWindowVisibleDisplayFrame(rect)

        // REMIND, you may like to change this using the fullscreen size of the phone
        // and also using the status bar and navigation bar heights of the phone to calculate
        // the keyboard height. But this worked fine on a Nexus.
        val orientation = screenOrientation
        val keyboardHeight = screenSize.y - rect.bottom
        if (lastKeyboardHeight == keyboardHeight) {
            return
        }
        lastKeyboardHeight = keyboardHeight
        notifyKeyboardHeightChanged(keyboardHeight, orientation)
        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                this.keyboardPortraitHeight = keyboardHeight
            }
            else -> {
                this.keyboardLandscapeHeight = keyboardHeight
            }
        }
    }

    /**
     *
     */
    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        if (observer != null) {
            observer!!.onKeyboardHeightChanged(height, orientation)
        }
    }

    companion object {

        /** The tag for logging purposes  */
        private val TAG = "sample_KeyboardHeightProvider"
    }
}