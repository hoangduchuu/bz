package com.ping.android.presentation.view.custom.newSticker

import android.content.Context



import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.bzzzchat.extensions.inflate



import com.ping.android.R
import kotlinx.android.synthetic.main.view_list_sticker.view.*


class StickerView : LinearLayout {


    lateinit var mContext: Context
    lateinit var categoryPath: String

    var urlList = ArrayList<String>()

    constructor(context: Context, categoryPath: String, pagePosition: Int) : super(context) {
        mContext = context

        this.categoryPath = categoryPath
        urlList = getListUrlStickerFactory(categoryPath, pagePosition)
        this.initView(pagePosition)

    }


    private fun initView(pagePosition: Int) {

        inflate(R.layout.view_list_sticker, true)
        val rvStickers: RecyclerView = this.findViewById(R.id.listStickers)
        rvStickers.layoutManager = GridLayoutManager(mContext, 5, RecyclerView.VERTICAL, false) as RecyclerView.LayoutManager?
        val stickerAdapter = StickerAdapterV2(mContext, urlList)
        rvStickers.adapter = stickerAdapter;

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
        return arrayListOf("","","")
    }
}