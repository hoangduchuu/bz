package com.ping.android.presentation.view.custom

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.vanniktech.emoji.*
import com.vanniktech.emoji.listeners.OnEmojiClickListener
import com.vanniktech.emoji.listeners.OnEmojiLongClickListener

class EmojiContainerView {
    lateinit var recentEmoji: RecentEmoji
    lateinit var variantEmoji: VariantEmoji
    lateinit var variantPopup: EmojiVariantPopup
    lateinit var emojiView: EmojiView

    fun init(context: Context, rootView: ViewGroup, editInterface: EmojiEditTextInterface?) {
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
        emojiView = EmojiView(context, clickListener, longClickListener, recentEmoji, variantEmoji)
        emojiView.setOnEmojiBackspaceClickListener { v ->
            editInterface?.backspace()
        }
    }

    fun dismiss() {
        emojiView.visibility = View.GONE
        variantPopup.dismiss()
    }

    fun show(currentHeight: Int) {
        emojiView.layoutParams.let {
            it.height = currentHeight
        }
        emojiView.visibility = View.VISIBLE
    }
}