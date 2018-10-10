package com.ping.android.presentation.view.adapter.sticker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ping.android.R
import kotlinx.android.synthetic.main.item_sticker.view.*

/**
 * Created by hoangduchuuvn@gmail.com on 10/9/18 .
 */
class StickerCategoryAdapter(val stickers: ArrayList<Int>, val context: Context) : RecyclerView.Adapter<StickerCategoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sticker, parent, false))
    }

    override fun getItemCount(): Int {
        return stickers.size - 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        setEmmiter()
        holder.ivSticker.setImageDrawable(context.getDrawable(stickers[position]))


//        holder.ivSticker.setOnClickListener { stickerEmmiter.onStickerSelected(position) }
    }


    class StickerTypeViewHolder()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSticker = view.ivSticker
    }

//    public fun setEmmiter(emmiter: StickerEmmiter) {
//        this.stickerEmmiter = emmiter
//    }

}