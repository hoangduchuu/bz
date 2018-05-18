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
import android.os.Bundle
import android.widget.Toast

import com.bzzzchat.videorecorder.R

class VideoRecorderActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_recorder)
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.record_container, Camera2VideoFragment.newInstance())
                    .commit()
        }
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