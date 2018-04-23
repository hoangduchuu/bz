package com.ping.android.presentation.view.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.ping.android.model.Message
import com.ping.android.presentation.view.fragment.ImageFragment

class ImagePagerAdapter(fragmentManager: FragmentManager, var messages: List<Message>): FragmentStatePagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment = ImageFragment.newInstance(messages[position])

    override fun getCount(): Int = messages.size
}