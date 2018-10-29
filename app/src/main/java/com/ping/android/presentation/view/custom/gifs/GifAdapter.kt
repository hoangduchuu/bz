package com.ping.android.presentation.view.custom.gifs

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bzzzchat.configuration.GlideApp
import com.ping.android.R
import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.GifTapEvent
import kotlinx.android.synthetic.main.item_gif.view.*


class GifAdapter(val context: Context,
                 var listUrlGifs: ArrayList<String>,
                val busProvider: BusProvider) : RecyclerView.Adapter<GifAdapter.ViewHolder>() {
    private lateinit var glide: RequestManager
    private val event = GifTapEvent()
    private lateinit var emmiter:GifsEmmiter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_gif, parent, false))
    }

    override fun getItemCount(): Int {
        return listUrlGifs.size
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val url = listUrlGifs[position].replace("http", "https").replace(" ","")
        GlideApp.with(context)
                .asGif()
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.img_loading_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(400, 400)
                .error(R.drawable.ic_error_outline)
                .fitCenter()
                .into(holder.ivGifs)

        // we emmit url here and handle in ChatActivity
        holder.ivGifs.setOnClickListener {
            emmiter.onGifselected(url)
        }
    }

public fun setEmmiter(e: GifsEmmiter){
    this.emmiter = e
}

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivGifs = view.ivGif
    }


}