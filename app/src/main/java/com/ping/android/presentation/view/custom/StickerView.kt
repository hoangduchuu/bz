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
import com.google.android.material.tabs.TabLayout
import com.ping.android.R
import com.ping.android.presentation.view.adapter.sticker.StickerAdapter
import com.ping.android.utils.Log
import kotlinx.android.synthetic.main.view_stickers.view.*

import kotlin.collections.ArrayList


interface ScrollListener {
    fun onScroll(postion: Int)
}

class StickerView : LinearLayout, TabLayout.OnTabSelectedListener, ScrollListener, StickerEmmiter {

    lateinit var scrollListener: ScrollListener

    lateinit var mContext: Context
    lateinit var adapter: StickerAdapter
    lateinit var rvStickers: RecyclerView
    private var stickerEmmiter: StickerEmmiter? = null

    private var isUserScrolling = false
    private var isListGoingUp = true
    private var isAutoScroll = false

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
    val stickerLists = ArrayList<String>()
    var folderStickerLists = ArrayList<String>()


    fun addFolderSticker() {
        folderStickerLists.add("biscuit")
        folderStickerLists.add("doraemon")
        folderStickerLists.add("helloKitty")
        folderStickerLists.add("meep")
        folderStickerLists.add("pikachu")
        folderStickerLists.add("pusheen")
        folderStickerLists.add("snoopyAtWork")
        folderStickerLists.add("xMyMelody")
    }

    fun addStickerToCollection(path: String) {
        val res = resources //if you are in an activity
        val am = res.assets
        val stickerList = am.list("stickers/pikachu")
        for (i in stickerList.indices) {
            stickerLists.add("$path/${stickerList[i]}")
        }

    }


    private fun showSticker1(t: View?) {

        addFolderSticker()
        for (i in folderStickerLists.indices) {
            addStickerToCollection(folderStickerLists[i])
        }



        cloneView2 = ImageView(context)
        cloneView2.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT) as ViewGroup.LayoutParams?
        cloneView2.setImageResource(R.mipmap.ic_launcher)


        adapter = StickerAdapter(stickerItems, mContext, this, this, stickerLists)


        rvStickers = findViewById(R.id.rvStickers)
        rvStickers.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)


        rvStickers.layoutManager = GridLayoutManager(context, 2, LinearLayoutManager.HORIZONTAL, false)
        rvStickers.adapter = adapter

        adapter.setListener(this, this)

        rvLayoutManaager = rvStickers.getLayoutManager() as GridLayoutManager

        tabStk.addTab(tabStk.newTab().setText("Tab 1"));
        tabStk.addTab(tabStk.newTab().setText("Tab 2"));
        tabStk.addTab(tabStk.newTab().setText("Tab 3"));
        tabStk.addTab(tabStk.newTab().setText("Tab 4"));
        tabStk.addTab(tabStk.newTab().setText("Tab 5"));
        tabStk.addTab(tabStk.newTab().setText("Tab 6"));
        tabStk.addTab(tabStk.newTab().setText("Tab 7"));

        tabStk.addOnTabSelectedListener(this)
        rvStickers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.e("addOnScrollListener onscrooled $dx --- $dy")
                isUserScrolling = false
                isListGoingUp = false
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.e("addOnScrollListener onScrollStateChanged $newState")
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isAutoScroll = false
                }
            }
        })
    }


    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.initView()
    }


    override fun onTabReselected(p0: TabLayout.Tab?) {

    }

    override fun onTabUnselected(p0: TabLayout.Tab?) {

    }

    override fun onTabSelected(p0: TabLayout.Tab?) {

        if (isUserScrolling) {
            return
        }

        isAutoScroll = true
        when (p0?.position) {
            0 -> rvStickers.smoothScrollToPosition(0)
            1 -> rvStickers.smoothScrollToPosition(34) // do
            2 -> rvStickers.smoothScrollToPosition(58) // kitty
            3 -> rvStickers.smoothScrollToPosition(82) // meep
            4 -> rvStickers.smoothScrollToPosition(106) // pika
            5 -> rvStickers.smoothScrollToPosition(130) // pusheen
            6 -> rvStickers.smoothScrollToPosition(154) // snoopy
            7 -> rvStickers.smoothScrollToPosition(178) // xMyMelo
            else -> {

            }
        }
    }

    lateinit var rvLayoutManaager: LinearLayoutManager


    override fun onScroll(postion: Int) {
        if (isAutoScroll ){return}
            isUserScrolling = true
        val positionView = (rvStickers.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

        Log.e("compare : $positionView vs $postion")
        if (positionView <=20){
            tabStk.post { tabStk.getTabAt(0)?.select() }
        }
        if (positionView in 22..46){
            tabStk.post { tabStk.getTabAt(1)?.select() }
        }
        if (positionView in 47..70){
            tabStk.post { tabStk.getTabAt(2)?.select() }
        }
        if (positionView in 71..95){
            tabStk.post { tabStk.getTabAt(3)?.select() }
        }
        if (positionView in 96..119){
            tabStk.post { tabStk.getTabAt(4)?.select() }
        }
        if (positionView in 120..143){
            tabStk.post { tabStk.getTabAt(5)?.select() }
        }
        if (positionView in 144..170){
            tabStk.post { tabStk.getTabAt(6)?.select() }
        }
        if (positionView >171){
            tabStk.post { tabStk.getTabAt(7)?.select() }
        }



    }

    /**
     * callback to EmojContainerView
     */
    override fun onStickerSelected(stickerPath: String, position: Int) {
        stickerEmmiter?.onStickerSelected(stickerPath,position)
    }

    fun setEmmitter(emmiter: StickerEmmiter) {
        this.stickerEmmiter = emmiter
    }
}