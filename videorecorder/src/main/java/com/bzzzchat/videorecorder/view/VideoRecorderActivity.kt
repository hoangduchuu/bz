/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bzzzchat.videorecorder.view

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast

import com.bzzzchat.videorecorder.R
import java.io.File

class VideoRecorderActivity : Activity() {
    private var extras: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_video_recorder)
        extras = intent.extras
        if (savedInstanceState == null) {
            openFragment(Camera2VideoFragment.newInstance())
        }
    }

    fun openFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .add(R.id.record_container, fragment)
                .addToBackStack(fragment.toString())
                .commit()
    }

    fun openPreviewPicture(file: File) {
        if (extras == null) {
            extras = Bundle()
        }
        extras!!.putString("imgPath", file.absolutePath)
        openFragment(PicturePreviewFragment.newInstance(extras!!))
    }

    override fun onBackPressed() {
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
            return
        }
        super.onBackPressed()
    }
}

/**
 * Shows a [Toast] on the UI thread.
 *
 * @param text The message to show
 */
fun Activity.showToast(text: String) {
    runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
}