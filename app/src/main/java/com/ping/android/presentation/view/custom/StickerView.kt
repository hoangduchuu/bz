package com.ping.android.presentation.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.ping.android.presentation.view.adapter.sticker.StickerAdapter
import com.ping.android.presentation.view.adapter.sticker.StickerCategoryAdapter
import kotlinx.android.synthetic.main.view_stickers.view.*
import java.util.*

class StickerView : LinearLayout {
    lateinit var mContext: Context
    lateinit var adapter: StickerAdapter
    lateinit var rvStickers: RecyclerView
    private var stickerEmmiter: StickerEmmiter? = null

    constructor(context: Context) : super(context) {
        mContext = context

        this.initView()
    }

    public fun initView() {
        inflate(R.layout.view_stickers, true)
        showSticker1(null)
    }

    lateinit var cloneView2: ImageView
    val stickerItems = ArrayList<Int>()

    private fun showSticker1(t: View?) {
        // fake
        for (i in 1..4) {
            stickerItems.add(R.drawable.ic_avatar_color)
            stickerItems.add(R.drawable.ic_add)
            stickerItems.add(R.drawable.ic_add_filled)
            stickerItems.add(R.drawable.ic_arrow_up)
            stickerItems.add(R.drawable.ic_arrow_right)
            stickerItems.add(R.drawable.ic_arrow_up)
            stickerItems.add(R.drawable.ic_block_outline)
            stickerItems.add(R.drawable.ic_error_outline)
            stickerItems.add(R.drawable.ic_error_outline)
        }

        cloneView2 = ImageView(context)
        cloneView2.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        cloneView2.setImageResource(R.mipmap.ic_launcher)

        clearAllView()


        adapter = StickerAdapter(stickerItems, mContext)


        rvStickers = RecyclerView(mContext)
        rvStickers.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)


        rvStickers.layoutManager = LinearLayoutManager(context)
        rvStickers.layoutManager = GridLayoutManager(context, 3)
        rvStickers.adapter = adapter
        sticker_content.addView(rvStickers)
        setupStickerCategory()
    }

    private fun showSticker2(t: View?) {
        Collections.shuffle(stickerItems)
        adapter.notifyDataSetChanged()
    }


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.initView()
    }

    private fun clearAllView() {
        sticker_content.removeAllViews()
    }

    lateinit var categoryAdapter: StickerCategoryAdapter
    var stickerCategory = ArrayList<Int>()

    private fun setupStickerCategory(){
        stickerCategory = stickerItems
        categoryAdapter  = StickerCategoryAdapter(stickerCategory,context)




//        rvStickerCategory.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        rvStickerCategory.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        rvStickerCategory.adapter = categoryAdapter
    }
    private fun getStickerData() {

    }
}