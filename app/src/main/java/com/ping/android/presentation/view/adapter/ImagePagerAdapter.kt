package com.ping.android.presentation.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.ping.android.model.Message
import com.ping.android.presentation.view.fragment.ImageFragment

class ImagePagerAdapter(fragmentManager: androidx.fragment.app.FragmentManager, var messages: MutableList<Message>): androidx.fragment.app.FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): androidx.fragment.app.Fragment = ImageFragment.newInstance(messages[position])

    override fun getCount(): Int = messages.size

    override fun getItemPosition(`object`: Any): Int {
        return androidx.viewpager.widget.PagerAdapter.POSITION_NONE
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