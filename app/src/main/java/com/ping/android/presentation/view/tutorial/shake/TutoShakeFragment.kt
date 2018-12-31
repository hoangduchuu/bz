package com.ping.android.presentation.view.tutorial.shake;

import android.os.Bundle
import android.view.*
import com.ping.android.R
import com.ping.android.device.impl.ShakeEventManager
import com.ping.android.presentation.view.fragment.BaseFragment
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.item_tutorial_chat_left_msg.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.core.view.GestureDetectorCompat
import com.ping.android.presentation.view.custom.GestureDetectorListener
import com.ping.android.utils.Log


/**
 * Created by Huu Hoang on 27/12/2018
 */
class TutoShakeFragment : BaseFragment(), TutoShakeContract.View,
GestureDetectorListener.GestureDetectorCallback {

    //region variable region

    /**
     * shakeEventManager: ShakeEventManager
     */
    private val shakeEventManager: ShakeEventManager by lazy {
        ShakeEventManager(context!!)
    }
    var gDetector: GestureDetectorCompat? = null

    lateinit var gListener : GestureDetectorListener


    private var maskedMsgText = "👿 👐👩‍🍳👩😎 👙💇‍♂️👩😎 💇‍♂️👩‍🍳😘 👒👩‍🍳, 👿💇‍♂️👩‍👿 😲😎👩, 👒💇‍♂️👩‍🍳👿😲 👙👩😎 😂👩😎 👆😎👩‍🔧👙 😂😎👿💇‍♂️ 👩‍🍳😃 😂😃👿 👩‍🍳👙👐 😘😃 👒😃👿 👐👩‍🍳 👆😃👿😲 👐😎👩‍🔧😂"

    /**
     *
     */
    private var isMasked = true;

    // endregion

    //region Android life cycle region

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        registerEvent(shakeEventManager.getShakeEvent()
                .debounce(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { handleShakePhone() })

    }

    private fun handleShakePhone() {
        changeTextMesage()
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tuto_shake,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        gListener = GestureDetectorListener(this)
        gDetector = GestureDetectorCompat(context,gListener)
        item_chat_text.setOnTouchListener(object : View.OnTouchListener {

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                gDetector?.onTouchEvent(event)
                return true;
            }

        })
    }

    override fun onResume() {
        super.onResume()
        shakeEventManager.register()
    }

    override fun onPause() {
        super.onPause()
        shakeEventManager.unregister()
    }

    // endregion


    //region override method region
    override fun showLoading() {
        super<BaseFragment>.showLoading()
    }

    override fun hideLoading() {
        super<BaseFragment>.hideLoading()
    }

    override fun onSingleTap() {}

    override fun onDoubleTap() {
        changeTextMesage()
    }

    override fun onLongPress() {}

    // endregion

    //region  private region

    private fun changeTextMesage() {
        if (isMasked) {
            isMasked = false
            item_chat_text.text = maskedMsgText
        } else {
            isMasked = true
            item_chat_text.text = resources.getString(R.string.content_messages)
        }
    }
    // endregion
}