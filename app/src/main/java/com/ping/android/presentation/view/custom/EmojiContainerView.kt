package com.ping.android.presentation.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.vanniktech.emoji.*
import com.vanniktech.emoji.listeners.OnEmojiClickListener
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener
import kotlinx.android.synthetic.main.view_emo.view.*

interface StickerEmmiter {
    fun onStickerSelected(stickerPath: String, position:Int)
}

interface GiftEmmiter {
    fun onGiftSelected(gifId: String)
}

class EmojiContainerView : LinearLayout, StickerEmmiter {


    lateinit var recentEmoji: RecentEmoji
    lateinit var variantEmoji: VariantEmoji
    lateinit var variantPopup: EmojiVariantPopup
    lateinit var emojiView: EmojiView
    private lateinit var edMessage: EmojiGifEditText
    lateinit var mContext: Context
    private var stickerEmmiter: StickerEmmiter? = null
    private var giftEmmiter: GiftEmmiter? = null
    private lateinit var container: ConstraintLayout


    var viewList = ArrayList<TextView>()

    constructor(context: Context) : super(context) {
        this.initView()
        mContext = context
    }

    lateinit var cloneView2: ImageView
    //    lateinit var cloneView3: ImageView
    lateinit var rvStickers: RecyclerView

    private fun initView() {
        inflate(R.layout.view_emo, true)
        bte1.setOnClickListener { t -> showStickerSection(t) }
        bte2.setOnClickListener { t -> showGiftSection(t) }
        bte3.setOnClickListener { t -> showEmojSection(t) }

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

    fun show(currentHeight: Int, container: ConstraintLayout, edMessage: EmojiGifEditText, stickerEmmiter: StickerEmmiter) {
        this.stickerEmmiter = stickerEmmiter
        this.container = container
        this.edMessage = edMessage
        setSelectedColor(bte1)

        root_container.visibility = View.VISIBLE
        root_container.layoutParams.let {
            it.height = currentHeight
        }
        clearAllView()
        initStickers2()


    }

    private fun showEmojSection(view: View) {
        setSelectedColor(view)
        clearAllView()
        initEmoj(context, container, edMessage)


    }

    private fun showGiftSection(v: View) {
        setSelectedColor(v)
        clearAllView()
        initGifts()
    }

    private fun showStickerSection(v: View) {
        setSelectedColor(v)
        clearAllView()
        initStickers2()
    }

    lateinit var stickerView: StickerView

    private fun initStickers2() {
        stickerView = StickerView(context)
        registerEmmiteroStickerView()
        emo_content.addView(stickerView)

    }
    
    private fun initEmoj(context: Context, rootView: ViewGroup, editInterface: EmojiEditTextInterface?) {
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
        emo_content.addView(emojiView)
    }

    private fun initGifts() {
        cloneView2 = ImageView(context)
        cloneView2.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        cloneView2.setImageResource(R.mipmap.ic_launcher)

        cloneView2.setOnClickListener { giftEmmiter?.onGiftSelected("https://i-thethao.vnecdn.net/2018/10/09/anhtop-1539054818-9688-1539054825_r_140x84.jpg") }

        emo_content.addView(cloneView2)
    }


    public fun setStickerEmmiter(emmiter: StickerEmmiter) {
        this.stickerEmmiter = emmiter
    }

    public fun setGifsEmmiter(emmiter: GiftEmmiter) {
        this.giftEmmiter = emmiter
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

    /**
     *   callback to ChatActivity
     */
    override fun onStickerSelected(stickerPath: String, position: Int) {
        stickerEmmiter?.onStickerSelected(stickerPath, position)
    }

    /**
     * register listener to stickerView
     */
    private fun registerEmmiteroStickerView() {
        stickerView.setEmmitter(this)
    }

}