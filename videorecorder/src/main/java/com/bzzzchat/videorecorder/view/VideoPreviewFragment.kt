package com.bzzzchat.videorecorder.view

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v4.media.session.MediaSessionCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bzzzchat.videorecorder.BuildConfig
import com.bzzzchat.videorecorder.R
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.fragment_video_preview.*
import java.io.File

private const val ARG_VIDEO_PATH = "ARG_VIDEO_PATH"

/**
 * A simple [Fragment] subclass.
 * Use the [VideoPreviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class VideoPreviewFragment : Fragment() {
    private var videoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoPath = it.getString(ARG_VIDEO_PATH)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_preview, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // While the user is in the app, the volume controls should adjust the music volume.
        initializePlayer(getUriFromFile(activity, File(videoPath)))
        btnBack.setOnClickListener { activity.onBackPressed() }
        btnSend.setOnClickListener {
            (activity as VideoRecorderActivity).onVideoSelected(videoPath!!)
        }
    }

    private fun initializePlayer(uri: Uri) {
        val player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(activity),
                DefaultTrackSelector(),
                DefaultLoadControl())
        videoPlayer.player = player
        player.playWhenReady = true
        player.seekTo(0)

        val mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, true, false)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        return ExtractorMediaSource.Factory(
                DefaultDataSourceFactory(activity, "bzzz-video"))
                .createMediaSource(uri)
    }

    private fun getUriFromFile(context: Context, file: File): Uri {
        val photoUri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            photoUri = FileProvider.getUriForFile(context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file)
            context.grantUriPermission("com.android.camera", photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            photoUri = Uri.fromFile(file)
        }
        return photoUri
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VideoPreviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(videoPath: String) =
                VideoPreviewFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_VIDEO_PATH, videoPath)
                    }
                }
    }
}
