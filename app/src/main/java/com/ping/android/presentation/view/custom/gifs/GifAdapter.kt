package com.ping.android.presentation.view.custom.gifs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.RequestManager
import com.bumptech.glide.annotation.GlideOption
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bzzzchat.configuration.GlideApp
import com.ping.android.R
import com.ping.android.utils.Log
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

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 10f
        circularProgressDrawable.centerRadius = 50f
        circularProgressDrawable.setColorSchemeColors(fetchAccentColor())
        circularProgressDrawable.start()


        val url = listUrlGifs[position].replace("http", "https").replace(" ","")
        GlideApp.with(context)
                .asGif()
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_error_outline)
                .listener(object : RequestListener<GifDrawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
                        return true
                    }
                    override fun onResourceReady(resource: GifDrawable?, model: Any?, target: Target<GifDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        calculateImageViewSize(resource?.intrinsicWidth!!.toFloat(), resource.intrinsicHeight.toFloat())
                        return false
                    }

                    private fun calculateImageViewSize(w: Float, h: Float) {
                        val fixedH = holder.ivGifs.height
                        val imageViewW = ((w/  h )* fixedH).toInt()
                        val params =  holder.ivGifs?.layoutParams;
                        params?.height = fixedH
                        params?.width = imageViewW
                        holder.ivGifs?.layoutParams = params
                    }
                })
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
//helper
private fun fetchAccentColor(): Int {
    val typedValue = TypedValue()

    val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
    val color = a.getColor(0, 0)

    a.recycle()

    return color
}

}