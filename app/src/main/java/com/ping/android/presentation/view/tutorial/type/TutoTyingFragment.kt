package com.ping.android.presentation.view.tutorial.type;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ping.android.R
import com.ping.android.presentation.view.fragment.BaseFragment

/**
 * Created by Huu Hoang on 27/12/2018
 */
class TutoTyingFragment : BaseFragment() {

    //region variable region

    // endregion

    //region Android life cycle region
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tuto_typing, container, false)
    }

    // endregion


    //region override method region
    override fun showLoading() {
        super<BaseFragment>.showLoading()
    }

    override fun hideLoading() {
        super<BaseFragment>.hideLoading()
    }
    // endregion

}