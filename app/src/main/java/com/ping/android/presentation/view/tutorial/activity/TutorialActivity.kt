package com.ping.android.presentation.view.tutorial.activity

import android.os.Bundle
import com.ping.android.R
import com.ping.android.presentation.view.activity.CoreActivity
import com.ping.android.presentation.view.tutorial.more.TutoMoreFragment
import com.ping.android.presentation.view.tutorial.shake.TutoShakeFragment
import com.ping.android.presentation.view.tutorial.voice.TutoVoiceFragment
import com.ping.android.presentation.view.tutorial.type.TutoTyingFragment
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_tutorial.*
import javax.inject.Inject


/**
 * Created by Huu Hoang on 27/12/2018
 */
class TutorialActivity : CoreActivity(),
        TutorialContract.View {

    //region VARIABLES region

    @Inject
    lateinit var presenter: TutorialContract.Presenter

    lateinit var adapter: TutoFragmentAdapter


    //endregion

    //region LIFE CYCLE region

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        AndroidInjection.inject(this)

        setupViewPager()
    }

    private fun setupViewPager() {
        adapter = TutoFragmentAdapter(supportFragmentManager);
        adapter.addFrag(TutoShakeFragment(),"one")
        adapter.addFrag(TutoTyingFragment(),"002")
        adapter.addFrag(TutoVoiceFragment(),"003")
        adapter.addFrag(TutoMoreFragment(),"004")

        viewpager.adapter = adapter
        tab_layout.setupWithViewPager(viewpager,true);
    }
    // endregion

    //region OVERRIDE METHOD region

    override fun showLoading() {
        super<CoreActivity>.showLoading()
    }

    override fun hideLoading() {
        super<CoreActivity>.hideLoading()
    }

    // endregion
}