package com.ping.android.presentation.view.custom.gifs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.bzzzchat.configuration.GlideApp
import com.ping.android.R
import com.ping.android.utils.Log
import com.ping.android.utils.bus.BusProvider
import kotlinx.android.synthetic.main.item_gif.view.*
import java.io.File
import com.ping.android.R.id.imageView




class GifAdapter(val context: Context,
                 var listUrlGifs: ArrayList<String>,
                val busProvider: BusProvider) : RecyclerView.Adapter<GifAdapter.ViewHolder>() {
    private lateinit var glide: RequestManager


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
        holder.ivGifs.setOnClickListener {

        }
    }



    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivGifs = view.ivGif
    }


}