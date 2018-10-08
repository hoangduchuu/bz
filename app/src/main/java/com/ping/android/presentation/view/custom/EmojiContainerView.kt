package com.ping.android.presentation.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.vanniktech.emoji.*
import com.vanniktech.emoji.listeners.OnEmojiClickListener
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener
import kotlinx.android.synthetic.main.view_emo.view.*

class EmojiContainerView : LinearLayout {
    lateinit var recentEmoji: RecentEmoji
    lateinit var variantEmoji: VariantEmoji
    lateinit var variantPopup: EmojiVariantPopup
    lateinit var emojiView: EmojiView
    private lateinit var edMessage: EmojiGifEditText
    lateinit var mContext: Context


    var viewList = ArrayList<TextView>()

    constructor(context: Context) : super(context) {
        this.initView()
        mContext = context
    }

    lateinit var cloneView2: ImageView
    lateinit var cloneView3: ImageView
    private fun initView() {
        inflate(R.layout.view_emo, true)
        bte1.setOnClickListener { t -> show1(t) }
        bte2.setOnClickListener { t -> show2(t) }
        bte3.setOnClickListener { t -> show3(t!!) }

        viewList.add(bte1)
        viewList.add(bte2)
        viewList.add(bte3)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.initView()
    }


    fun dismiss() {
        root_container.visibility = View.INVISIBLE
    }

    fun show(currentHeight: Int, container: ConstraintLayout, edMessage: EmojiGifEditText) {
        setSelectedColor(bte1)
        init1(context, container, edMessage)
        root_container.visibility = View.VISIBLE
        root_container.layoutParams.let {
            it.height = currentHeight
        }
        clearAllView()
        emo_content.addView(emojiView)
    }

    private fun show1(view: View) {
        setSelectedColor(view)
        clearAllView()
        emo_content.addView(emojiView)
    }

    private fun show2(v: View) {
        setSelectedColor(v)
        clearAllView()
        init2()
    }

    private fun show3(v: View) {
        v.setBackgroundColor(android.graphics.Color.RED)
        setSelectedColor(v)
        clearAllView()
        init3()
    }

    private fun init1(context: Context, rootView: ViewGroup, editInterface: EmojiEditTextInterface?) {
        val clickListener = OnEmojiClickListener { imageView, emoji ->
            editInterface?.input(emoji)

            recentEmoji.addEmoji(emoji)
            variantEmoji.addVariant(emoji)
            imageView.updateEmoji(emoji)
            variantPopup.dismiss()
        }
        val longClickListener = OnEmojiLongClickListener { view, emoji ->
            variantPopup.show(view, emoji)
        }
        variantPopup = EmojiVariantPopup(rootView, clickListener)
        recentEmoji = RecentEmojiManager(context)
        variantEmoji = VariantEmojiManager(context)
        emojiView = EmojiView(context, clickListener, longClickListener, recentEmoji, variantEmoji, 0, 0, 0)
        emojiView.setOnEmojiBackspaceClickListener { v ->
            editInterface?.backspace()
        }
    }

    private fun init2() {
        cloneView2 = ImageView(context)
        cloneView2.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        cloneView2.setImageResource(R.mipmap.ic_launcher)
        emo_content.addView(cloneView2)
    }

    private fun init3() {
        cloneView3 = ImageView(context)
        cloneView3.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        GlideApp.with(this)
                .asBitmap()
                .load("https://i-thethao.vnecdn.net/2018/10/09/anhtop-1539054818-9688-1539054825_r_140x84.jpg")
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(cloneView3)
        emo_content.addView(cloneView3)
    }

    private fun clearAllView() {
        emo_content.removeAllViews()
    }

    private fun setSelectedColor(view: View) {
        view.layoutParams
        for (i in 0 until viewList.size) {
            if (view.id != viewList[i].id) viewList[i].background = context.getDrawable(R.drawable.background_button_emo_)
            else viewList[i].background = context.getDrawable(R.drawable.background_button_emo_selected)
        }
    }
}