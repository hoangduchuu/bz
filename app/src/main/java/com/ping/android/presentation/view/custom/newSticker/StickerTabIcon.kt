package com.ping.android.presentation.view.custom.newSticker

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.ping.android.utils.Log
import kotlinx.android.synthetic.main.view_sticker_tab_icon.view.*
import java.io.InputStream


class StickerTabIcon : LinearLayout {

    lateinit var mContext: Context

    constructor(context: Context, folderName: String, isRecentIcon: Boolean) : super(context) {
        mContext = context

        this.initView(context, folderName, isRecentIcon)
    }
    constructor(context: Context, data: StickerData, position:Int) : super(context) {
        mContext = context

        this.initView(context, data, position)
    }
    private fun initView(context: Context, data: StickerData,position: Int){
        inflate(R.layout.view_sticker_tab_icon, true)
        if(position ==0){
            ivTabicon.setImageDrawable(context.getDrawable(R.drawable.emoji_recent))
            return
        }
        try {
            val img: InputStream = context.assets.open("stickers/${getFolderName(data,position)}/2.png")
            val d: Drawable = Drawable.createFromStream(img, null)
            ivTabicon.setImageDrawable(d)
        } catch (e: Exception) {

        }

    }
    private fun initView(context: Context, folderName: String, isRecentIcon: Boolean) {
        inflate(R.layout.view_sticker_tab_icon, true)
        if (isRecentIcon) {
            ivTabicon.setImageDrawable(context.getDrawable(R.drawable.emoji_recent))
            return
        }else{
            try {
                val img: InputStream = context.assets.open("stickers/$folderName/2.png")
                val d: Drawable = Drawable.createFromStream(img, null)
                ivTabicon.setImageDrawable(d)
            } catch (e: Exception) {

            }
        }

    }

    private fun getFolderName(data: StickerData, position: Int):String{
        return data.categoryList[position]
    }

    private fun getPresenIcon(data: StickerData,position: Int):String{
        return "${data.categoryList[position]}/2.png"
    }


}