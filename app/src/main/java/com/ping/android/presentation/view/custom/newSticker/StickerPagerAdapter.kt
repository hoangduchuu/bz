package com.ping.android.presentation.view.custom.newSticker

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.ping.android.utils.bus.BusProvider

class StickerPagerAdapter(private val mContext: Context, var data: StickerData, val busProvider: BusProvider) : PagerAdapter() {

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val stickerView = StickerView(mContext,data.categoryList[position], position,busProvider)
        collection.addView(stickerView)
        return stickerView
    }


    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return data.categoryList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return data.categoryList[position]
//        return ""
    }

}
