package com.ping.android.presentation.view.activity

import android.Manifest
import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.bzzzchat.videorecorder.view.*
import com.ping.android.R
import java.io.File

class FaceTrainingActivity : Activity() {

    private var extras: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(com.bzzzchat.videorecorder.R.layout.activity_video_recorder)
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
                .add(com.bzzzchat.videorecorder.R.id.record_container, fragment)
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
            val currentFragment = fragmentManager.findFragmentById(com.bzzzchat.videorecorder.R.id.record_container)
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
        intent.putExtra(VideoRecorderActivity.IMAGE_EXTRA_KEY, imageFile)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun onVideoSelected(videoFile: String) {
        val intent = Intent()
        intent.putExtra(VideoRecorderActivity.VIDEO_EXTRA_KEY, videoFile)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun onPermissionGranted() {
        openFragment(Camera2BasicFragment())
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
}
