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

import android.Manifest
import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
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
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                onPermissionGranted()
            } else {
                if (isPermissionsGranted()) {
                    onPermissionGranted()
                } else {
                    requestPermissions()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        if (requestCode == 1212) {
            if (grantResults != null) {
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        onPermissionFailed()
                        return
                    }
                }
                onPermissionGranted()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun openFragment(fragment: Fragment) {
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

    fun openPreviewVideo(file: File) {
        openFragment(VideoPreviewFragment.newInstance(file.absolutePath))
    }

    override fun onBackPressed() {
        if (fragmentManager.backStackEntryCount > 1) {
            val currentFragment = fragmentManager.findFragmentById(R.id.record_container)
            if (currentFragment != null && currentFragment is VideoPreviewFragment) {
                currentFragment.onBackPress()
            }
            fragmentManager.popBackStackImmediate()
            return
        } else {
            val intent = Intent()
            intent.putExtra("data", false)
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
    }

    fun onImageSelected(imageFile: String) {
        val intent = Intent()
        intent.putExtra(IMAGE_EXTRA_KEY, imageFile)
        setResult(RESULT_OK, intent)
        finish()
    }

    fun onVideoSelected(videoFile: String) {
        val intent = Intent()
        intent.putExtra(VIDEO_EXTRA_KEY, videoFile)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun onPermissionGranted() {
        openFragment(Camera2VideoFragment.newInstance(extras!!))
    }

    private fun onPermissionFailed() {
        finish()
    }

    private fun isPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermissions() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO), 1212)
    }

    override fun finish() {
        super.finish()
    }

    companion object {
        const val OUTPUT_FOLDER_EXTRA_KEY = "OUTPUT_FOLDER_EXTRA_KEY"
        const val IMAGE_EXTRA_KEY = "IMAGE_EXTRA_KEY"
        const val VIDEO_EXTRA_KEY = "VIDEO_EXTRA_KEY"
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