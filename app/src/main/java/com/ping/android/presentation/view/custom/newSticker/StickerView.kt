package com.ping.android.presentation.view.custom.newSticker

import android.content.Context


import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.bzzzchat.extensions.inflate


import com.ping.android.R
import com.ping.android.utils.Log
import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.StickerTapEvent


class StickerView : LinearLayout, StickerClickListener {


    var mContext: Context
    var categoryPath: String
    lateinit var event: StickerTapEvent
    var busProvider: BusProvider
    var urlList = ArrayList<String>()

    constructor(context: Context, categoryPath: String, pagePosition: Int, busProvider: BusProvider) : super(context) {
        mContext = context
        this.busProvider = busProvider
        this.categoryPath = categoryPath
        urlList = getListUrlStickerFactory(categoryPath, pagePosition)
        this.initView(pagePosition, busProvider)

    }


    private fun initView(pagePosition: Int, busProvider: BusProvider) {

        inflate(R.layout.view_list_sticker, true)
        val rvStickers: RecyclerView = this.findViewById(R.id.listStickers)
        rvStickers.layoutManager = GridLayoutManager(mContext, 5, RecyclerView.VERTICAL, false) as RecyclerView.LayoutManager?
        val stickerAdapter = StickerAdapterV2(mContext, urlList)
        rvStickers.adapter = stickerAdapter;
        event = StickerTapEvent()
        stickerAdapter.setListener(this)

    }

    private fun getListUrlStickerFactory(folderName: String, pagePosition: Int): ArrayList<String> {
        var stickerPathList: ArrayList<String>

        if (pagePosition == 0) {
            stickerPathList = getListUrlStickerHistory()
        } else {
            stickerPathList = getLtUrlStickerByFolderName(folderName)
        }
        return stickerPathList

    }

    private fun getLtUrlStickerByFolderName(folderName: String): ArrayList<String> {
        var list = ArrayList<String>()
        val res = resources //if you are in an activity
        val am = res.assets
        val stickerList = am.list("stickers/$folderName")
        for (i in stickerList.indices) {
            list.add("stickers/$folderName/${stickerList[i]}")
        }
        return list
    }

    private fun getListUrlStickerHistory(): ArrayList<String> {
        return arrayListOf("", "", "")
    }

    override fun onClick(path: String) {
        event.path = path
        busProvider.post(event)

    }
}