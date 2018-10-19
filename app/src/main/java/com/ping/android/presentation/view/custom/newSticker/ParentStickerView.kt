package com.ping.android.presentation.view.custom.newSticker

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import kotlinx.android.synthetic.main.view_stickers.view.*

import kotlin.collections.ArrayList


interface ScrollListener {
    fun onScroll(postion: Int)
}

class ParentStickerView : LinearLayout{

    lateinit var mContext: Context

    constructor(context: Context) : super(context) {
        mContext = context

        this.initView()
    }


    private fun initView() {
        inflate(R.layout.view_stickers, true)

        val viewPager = view_pager

        val data = getCategories()

        val adapter = StickerPagerAdapter(mContext, data)
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

        val res = resources //if you are in an activity
        val am = res.assets
        val folders = am.list("stickers/")
        for (i in folders.indices) {
            folderName.add("${folders[i]}")
        }
        return StickerData(folderName)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.initView()
    }

}