package com.ping.android.presentation.view.adapter.sticker

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ping.android.R
import com.ping.android.presentation.view.custom.newSticker.ScrollListener
import com.ping.android.presentation.view.custom.StickerEmmiter
import kotlinx.android.synthetic.main.item_sticker.view.*
import java.io.InputStream


/**
 * Created by hoangduchuuvn@gmail.com on 10/9/18 .
 */
class StickerAdapter(val stickers: ArrayList<Int>,
                     val context: Context,
                     var scrollListener: ScrollListener
                     , var stickerEmmiter: StickerEmmiter, var stickerLists: ArrayList<String>) : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sticker, parent, false))
    }

    override fun getItemCount(): Int {
        return stickerLists.size - 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val img: InputStream = context.getAssets().open("stickers/" + stickerLists[position])
//
            val d: Drawable = Drawable.createFromStream(img, null)
            holder.ivSticker.setImageDrawable(d)
        } catch (e: Exception) {

        }
        scrollListener.onScroll(position)
        holder.ivSticker.setOnClickListener {
            stickerEmmiter.onStickerSelected(stickerLists[position], position)

        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSticker = view.ivSticker
    }


    public fun setListener(emmiter: ScrollListener, stickerEmmiter: StickerEmmiter) {
        this.scrollListener = emmiter
        this.stickerEmmiter = stickerEmmiter
    }


}