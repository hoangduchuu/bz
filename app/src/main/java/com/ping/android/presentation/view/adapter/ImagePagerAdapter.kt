package com.ping.android.presentation.view.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import com.ping.android.model.Message
import com.ping.android.presentation.view.fragment.ImageFragment

class ImagePagerAdapter(fragmentManager: FragmentManager, var messages: MutableList<Message>): FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment = ImageFragment.newInstance(messages[position])

    override fun getCount(): Int = messages.size

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    fun updateMessage(message: Message, position: Int) {
        messages[position] = message
        notifyDataSetChanged()
    }

    fun updateMessages(messages: List<Message>) {
        this.messages = ArrayList(messages)
        notifyDataSetChanged()
    }
}