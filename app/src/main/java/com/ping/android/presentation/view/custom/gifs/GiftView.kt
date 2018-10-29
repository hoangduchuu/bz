package com.ping.android.presentation.view.custom.gifs

import android.content.Context


import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.bzzzchat.extensions.inflate


import com.ping.android.R
import com.ping.android.utils.Log

import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.GifTapEvent
import com.ping.android.utils.bus.events.StickerTapEvent
import kotlin.collections.ArrayList

interface GifsEmmiter {
    fun onGifselected(url: String)
}

class GiftView : LinearLayout, GifsEmmiter {
    var mContext: Context
    lateinit var event: GifTapEvent
    var busProvider: BusProvider

    constructor(context: Context, busProvider: BusProvider) : super(context) {
        mContext = context
        this.busProvider = busProvider
        this.initView(busProvider)

    }


    private fun initView(busProvider: BusProvider) {
        inflate(R.layout.view_list_gifts, true)
        val rvGifts: RecyclerView = this.findViewById(R.id.listGifs)
        rvGifts.layoutManager = GridLayoutManager(mContext, 1, RecyclerView.HORIZONTAL, false)
        val data = ArrayList<String>()
        data.addAll(GifDataList.bem.getList())
        val gifAdapter = GifAdapter(mContext, data, busProvider)
        gifAdapter.setEmmiter(this)
        rvGifts.adapter = gifAdapter;
        event = GifTapEvent()
    }

    override fun onGifselected(url: String) {
        event.gifUrl = url
        busProvider.post(event)
    }


}