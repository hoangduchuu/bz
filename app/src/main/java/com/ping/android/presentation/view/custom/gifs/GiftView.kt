package com.ping.android.presentation.view.custom.gifs

import android.content.Context


import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.bzzzchat.extensions.inflate



import com.ping.android.R
import com.ping.android.data.entity.MessageEntity_Table
import com.ping.android.utils.Log

import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.StickerTapEvent
import com.raizlabs.android.dbflow.sql.language.SQLite
import java.util.*



class GiftView : LinearLayout {
    var mContext: Context
    lateinit var event: StickerTapEvent
    var busProvider: BusProvider

    constructor(context: Context ,busProvider: BusProvider) : super(context) {
        mContext = context
        this.busProvider = busProvider
        this.initView( busProvider)

    }



    private fun initView( busProvider: BusProvider) {
        inflate(R.layout.view_list_gifts, true)
        val rvGifts: RecyclerView = this.findViewById(R.id.listGifs)
        rvGifts.layoutManager = GridLayoutManager(mContext, 5, RecyclerView.VERTICAL, false) as RecyclerView.LayoutManager?
    }





}