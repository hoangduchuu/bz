package com.ping.android.presentation.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ping.android.R
import com.ping.android.presentation.view.custom.StickerEmmiter
import kotlinx.android.synthetic.main.item_sticker.view.*

/**
 * Created by hoangduchuuvn@gmail.com on 10/9/18 .
 */
class StickerAdapter(val stickers: ArrayList<Int>, val context: Context, var stickerEmmiter: StickerEmmiter) : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sticker, parent, false))
    }

    override fun getItemCount(): Int {
        return stickers.size - 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setEmmiter(stickerEmmiter)
        holder.ivSticker.setImageDrawable(context.getDrawable(stickers[position]))


        holder.ivSticker.setOnClickListener { stickerEmmiter.onStickerSelected(position) }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSticker = view.ivSticker
    }

    public fun setEmmiter(emmiter: StickerEmmiter) {
        this.stickerEmmiter = emmiter
    }

}