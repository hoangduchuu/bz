package com.bzzzchat.videorecorder.view

import android.os.Bundle
import android.app.Fragment
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.support.v4.content.FileProvider
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bzzzchat.videorecorder.BuildConfig

import com.bzzzchat.videorecorder.R
import com.bzzzchat.videorecorder.view.custom.MediaCatalog
import com.example.android.videoplayersample.PlayerHolder
import com.example.android.videoplayersample.PlayerState
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import kotlinx.android.synthetic.main.fragment_video_preview.*
import java.io.File
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.*
import org.jetbrains.anko.act


private const val ARG_VIDEO_PATH = "ARG_VIDEO_PATH"

/**
 * A simple [Fragment] subclass.
 * Use the [VideoPreviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class VideoPreviewFragment : Fragment() {
    private var videoPath: String? = null

//    private val mediaSession: MediaSessionCompat by lazy { createMediaSession() }
//    private val mediaSessionConnector: MediaSessionConnector by lazy {
//        createMediaSessionConnector()
//    }
//    private val playerState by lazy { PlayerState() }
//    private lateinit var playerHolder: PlayerHolder

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
        activity.volumeControlStream = AudioManager.STREAM_MUSIC
        //initializePlayer(getUriFromFile(activity, File(videoPath)))
        initializePlayer(Uri.parse("http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"))
        //createMediaSession()
//        createPlayer()
    }

//    override fun onStart() {
//        super.onStart()
        //startPlayer()
//        activateMediaSession()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        stopPlayer()
//        deactivateMediaSession()
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        releaseMediaSession()
//        releasePlayer()
//    }

    private fun create() {

    }

    private fun initializePlayer(uri: Uri) {
        val player = ExoPlayerFactory.newSimpleInstance(
                DefaultRenderersFactory(activity),
                DefaultTrackSelector(),
                DefaultLoadControl())
        videoPlayer.player = player
        //player.addListener(componentListener);
        player.playWhenReady = true
        player.seekTo(0)

        val mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, true, false)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        return ExtractorMediaSource.Factory(
                DefaultDataSourceFactory(activity, "exoplayer-learning"))
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

    // MediaSession related functions.
    private fun createMediaSession(): MediaSessionCompat = MediaSessionCompat(activity, activity.packageName)

//    private fun createMediaSessionConnector(): MediaSessionConnector =
//            MediaSessionConnector(mediaSession).apply {
//                // If QueueNavigator isn't set, then mediaSessionConnector will not handle following
//                // MediaSession actions (and they won't show up in the minimized PIP activity):
//                // [ACTION_SKIP_PREVIOUS], [ACTION_SKIP_NEXT], [ACTION_SKIP_TO_QUEUE_ITEM]
//                setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
//                    override fun getMediaDescription(windowIndex: Int): MediaDescriptionCompat {
//                        return MediaCatalog[windowIndex]
//                    }
//                })
//            }


    // MediaSession related functions.
//    private fun activateMediaSession() {
//        // Note: do not pass a null to the 3rd param below, it will cause a NullPointerException.
//        // To pass Kotlin arguments to Java varargs, use the Kotlin spread operator `*`.
//        mediaSessionConnector.setPlayer(playerHolder.audioFocusPlayer, null)
//        mediaSession.isActive = true
//    }
//
//    private fun deactivateMediaSession() {
//        mediaSessionConnector.setPlayer(null, null)
//        mediaSession.isActive = false
//    }
//
//    private fun releaseMediaSession() {
//        mediaSession.release()
//    }
//
//    // ExoPlayer related functions.
//    private fun createPlayer() {
//        playerHolder = PlayerHolder(activity, playerState, videoPlayer, File(videoPath))
//    }
//
//    private fun startPlayer() {
//        playerHolder.start()
//    }
//
//    private fun stopPlayer() {
//        playerHolder.stop()
//    }
//
//    private fun releasePlayer() {
//        playerHolder.release()
//    }

    // Picture in Picture related functions.
//    override fun onUserLeaveHint() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            activity.enterPictureInPictureMode(
//                    with(PictureInPictureParams.Builder()) {
//                        val width = 16
//                        val height = 9
//                        setAspectRatio(Rational(width, height))
//                        build()
//                    })
//        }
//    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean,
                                               newConfig: Configuration?) {
        videoPlayer.useController = !isInPictureInPictureMode
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
