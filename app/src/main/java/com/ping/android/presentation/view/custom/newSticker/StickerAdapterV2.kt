package com.ping.android.presentation.view.custom.newSticker

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ping.android.R
import kotlinx.android.synthetic.main.item_sticker.view.*
import java.io.InputStream

class StickerAdapterV2(val context: Context,
                       var stickerPathUrlArrays: ArrayList<String>) : RecyclerView.Adapter<StickerAdapterV2.ViewHolder>() {

   lateinit var mlistenner: StickerClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sticker, parent, false))
    }

    override fun getItemCount(): Int {
        return stickerPathUrlArrays.size
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val img: InputStream = context.assets.open(stickerPathUrlArrays[position])
//
            val d: Drawable = Drawable.createFromStream(img, null)
            holder.ivSticker.setImageDrawable(d)
        } catch (e: Exception) {

        }
        holder.ivSticker.setOnClickListener {
mlistenner.onClick(stickerPathUrlArrays[position])
        }

    }

    fun setListener(listener: StickerClickListener) {
        mlistenner = listener
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSticker = view.ivSticker
    }


}