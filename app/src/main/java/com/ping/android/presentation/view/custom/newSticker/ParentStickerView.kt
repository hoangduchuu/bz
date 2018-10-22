package com.ping.android.presentation.view.custom.newSticker

import android.content.Context
import android.widget.LinearLayout
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.ping.android.utils.bus.BusProvider
import kotlinx.android.synthetic.main.view_stickers.view.*

import kotlin.collections.ArrayList


interface ScrollListener {
    fun onScroll(postion: Int)
}

class ParentStickerView : LinearLayout{

    lateinit var mContext: Context
    lateinit var busProvider: BusProvider

    constructor(context: Context,busProvider: BusProvider) : super(context) {
        mContext = context
        this.busProvider = busProvider
        this.initView(busProvider)
    }


    private fun initView(busProvider: BusProvider) {
        inflate(R.layout.view_stickers, true)

        val viewPager = view_pager

        val data = getCategories()

        val adapter = StickerPagerAdapter(mContext, data,busProvider)
        viewPager.offscreenPageLimit = 0;

        viewPager.adapter = adapter


        tabs.setupWithViewPager(viewPager)


        /**
         * setUp tabicon
         */
        for (i in data.categoryList.indices){
            tabs.getTabAt(i)?.customView = StickerTabIcon(mContext,data,i)
        }
    }

    private fun getCategories(): StickerData {

        var folderName = ArrayList<String>()
        /**
         * the history categori
         */
        folderName.add("histories")
        folderName.add("biscuit")
        folderName.add("doraemon")
        folderName.add("helloKitty")
        folderName.add("meep")
        folderName.add("pikachu")
        folderName.add("pusheen")
        folderName.add("snoopyAtWork")
        folderName.add("xMyMelody")
//
//        val res = resources //if you are in an activity
//        val am = res.assets
//        val folders = am.list("stickers/")
//        for (i in folders.indices) {
//            folderName.add("${folders[i]}")
//        }
        return StickerData(folderName)
    }


}