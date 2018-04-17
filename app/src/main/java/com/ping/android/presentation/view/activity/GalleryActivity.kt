package com.ping.android.presentation.view.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ping.android.R

class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val transaction = supportFragmentManager.beginTransaction()
        //transaction.add(R.id.container, GridGalleryFragment.newInstance())
        transaction.commit()
    }
}
