package com.bzzzchat.videorecorder.view

import android.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bzzzchat.videorecorder.R

open class VideoPlayerActivity : AppCompatActivity() {
    private lateinit var videoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        intent.extras.apply {
            videoPath = getString(VIDEO_PATH_EXTRA_KEY)
        }

        openFragment(VideoPreviewFragment.newInstance(videoPath, isPreview = false), true)
    }

    private fun openFragment(fragment: Fragment, isRoot: Boolean) {
        val transaction = fragmentManager.beginTransaction()
                .add(R.id.container, fragment)
        if (!isRoot) {
            transaction.addToBackStack(fragment.toString())
        }
        transaction.commit()
    }

    companion object {
        const val VIDEO_PATH_EXTRA_KEY = "VIDEO_PATH_EXTRA_KEY"
    }
}
