package com.bzzzchat.videorecorder.view

import android.app.Fragment
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.content.FileProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bzzzchat.videorecorder.R
import com.coremedia.iso.IsoFile
import com.coremedia.iso.boxes.TrackBox
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.googlecode.mp4parser.DataSource
import com.googlecode.mp4parser.FileDataSourceImpl
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.Mp4TrackImpl
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import kotlinx.android.synthetic.main.fragment_video_preview.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel

private const val ARG_VIDEO_PATH = "ARG_VIDEO_PATH"
private const val ARG_IS_PREVIEW = "ARG_IS_PREVIEW"

/**
 * A simple [Fragment] subclass.
 * Use the [VideoPreviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class VideoPreviewFragment : Fragment() {
    private var videoPath: String? = null
    private var isPreview: Boolean = true
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoPath = it.getString(ARG_VIDEO_PATH)
            isPreview = it.getBoolean(ARG_IS_PREVIEW, true)
        }
        val manufacturer = android.os.Build.MANUFACTURER
        if (manufacturer == "samsung" && isPreview) {
            fixSamsungBug()
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
        if (isPreview) {
            btnSend.visibility = View.VISIBLE
            videoPlayer.useController = true
            btnSend.setOnClickListener {
                (activity as VideoRecorderActivity).onVideoSelected(videoPath!!)
            }
        } else {
            btnSend.visibility = View.GONE
            videoPlayer.useController = true
        }
    }

    private fun initializePlayer(uri: Uri) {
        player = ExoPlayerFactory.newSimpleInstance(
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

    private fun getAuthority(): String {
        return activity.application.packageName + getString(R.string.provider_package);
    }

    private fun getUriFromFile(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context,
                    getAuthority(),
                    file)
        } else {
            Uri.fromFile(file)
        }
    }

    override fun onPause() {
        super.onPause()
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    fun onBackPress() {
        if (videoPath != null && isPreview) {
            val file = File(videoPath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun fixSamsungBug() {
        var channel: DataSource? = null
        try {
            channel = FileDataSourceImpl(videoPath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        var isoFile: IsoFile? = null

        try {
            isoFile = IsoFile(channel)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val trackBoxes = isoFile!!.movieBox.getBoxes(TrackBox::class.java)
        var sampleError = false
        for (trackBox in trackBoxes) {
            val firstEntry = trackBox.mediaBox.mediaInformationBox
                    .sampleTableBox.timeToSampleBox.entries[0]

            // Detect if first sample is a problem and fix it in isoFile
            // This is a hack. The audio deltas are 1024 for my files, and video deltas about 3000
            // 10000 seems sufficient since for 30 fps the normal delta is about 3000
            if (firstEntry.delta > 10000) {
                sampleError = true
                firstEntry.delta = 3000
            }
        }

        if (sampleError) {
            Log.d("gpinterviewandroid", "Sample error! correcting...");
            val movie = Movie()
            for (trackBox in trackBoxes) {
                movie.addTrack(Mp4TrackImpl(channel.toString() + "[" + trackBox.trackHeaderBox.trackId + "]", trackBox))
            }
            movie.matrix = isoFile.movieBox.movieHeaderBox.matrix
            val out = DefaultMp4Builder().build(movie)

            //delete file first!
            val file = File(videoPath)
            val deleted = file.delete()


            var fc: FileChannel? = null
            try {
                //fc = new FileOutputStream(new File(app.dataMgr.videoFileURL)).getChannel();
                fc = RandomAccessFile(videoPath, "rw").channel
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            try {
                out.writeContainer(fc)
                fc?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            Log.d("gpinterviewandroid", "Finished correcting raw video");
        }
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
        fun newInstance(videoPath: String, isPreview: Boolean = true) =
                VideoPreviewFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_VIDEO_PATH, videoPath)
                        putBoolean(ARG_IS_PREVIEW, isPreview)
                    }
                }
    }
}
