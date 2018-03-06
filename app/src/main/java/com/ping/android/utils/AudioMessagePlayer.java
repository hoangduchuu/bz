package com.ping.android.utils;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ping.android.activity.R;
import com.ping.android.model.Message;
import com.ping.android.ultility.CommonMethod;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by bzzz on 12/6/17.
 */

public class AudioMessagePlayer implements SeekBar.OnSeekBarChangeListener, View.OnClickListener{
    private final String TAG = this.getClass().getSimpleName();

    private static final int AUDIO_PROGRESS_UPDATE_TIME = 100;
    private static Message message;
    private static int totalTime = 0;
    private static View itemView;

    // TODO: externalize the error messages.
    private static final String ERROR_PLAYVIEW_NULL = "Play view cannot be null";
    private static final String ERROR_PLAYTIME_CURRENT_NEGATIVE = "Current playback time cannot be negative";
    private static final String ERROR_PLAYTIME_TOTAL_NEGATIVE = "Total playback time cannot be negative";

    private Handler mProgressUpdateHandler;

    private MediaPlayer mMediaPlayer;

    private static AudioMessagePlayer mAudioMessagePlayer;

    public void onProgressChange(int currentTime, int totalTime, boolean isFromSeekBar) {
        TextView mRunTime = itemView.findViewById(R.id.playback_time);
        SeekBar mMediaSeekBar = itemView.findViewById(R.id.media_seekbar);
        StringBuilder playbackStr = new StringBuilder();
        int remainingTime = totalTime - currentTime;

        // set the current time
        // its ok to show 00:00 in the UI
        playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) remainingTime),
                TimeUnit.MILLISECONDS.toSeconds((long) remainingTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) remainingTime))));
        if (mRunTime != null) {
            mRunTime.setText(playbackStr.toString());
        }
        if (!isFromSeekBar && mMediaSeekBar != null) {
            mMediaSeekBar.setProgress(currentTime);
        }

        if (currentTime == totalTime){
            setPlayable(true);
        }
    }

    private AudioMessagePlayer(){
    }


    public static AudioMessagePlayer getInstance() {

        if (mAudioMessagePlayer == null) {
            mAudioMessagePlayer = new AudioMessagePlayer();
        }

        return mAudioMessagePlayer;
    }

    private Runnable mUpdateProgress = new Runnable() {

        public void run() {

            if (mProgressUpdateHandler != null && mMediaPlayer.isPlaying()) {
                onProgressChange(mMediaPlayer.getCurrentPosition(), totalTime, false);

                // repeat the process
                mProgressUpdateHandler.postDelayed(this, AUDIO_PROGRESS_UPDATE_TIME);
            } else {
                // DO NOT update UI if the player is paused
            }
        }
    };

    public void play() {

        if (mMediaPlayer == null) {
            throw new IllegalStateException("Call init() before calling this method");
        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }

        mProgressUpdateHandler.postDelayed(mUpdateProgress, AUDIO_PROGRESS_UPDATE_TIME);
        mMediaPlayer.start();
        setPausable();
    }

    public void pause() {

        if (mMediaPlayer == null) {
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        setPlayable(false);
    }

    public AudioMessagePlayer initPlayer(View itemView, Message message) {
        //skip init if same message has been set
        if( this.message == message){
            return this;
        }

        //if media is play then stop, move seekbar to start
        if(this.itemView != null) {
            setPlayable(true);
        }

        //change current audio record
        this.itemView = itemView;

        this.message = message;
        this.totalTime = getTotalTime();
        setPlayable(true);

        if(mMediaPlayer != null) {
            release();
        }
        mMediaPlayer = new MediaPlayer();
        mProgressUpdateHandler = new Handler();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        String audioLocalPath = itemView.getContext().getExternalFilesDir(null).getAbsolutePath() + File.separator
                + CommonMethod.getFileNameFromFirebase(message.audioUrl);

        try {
            mMediaPlayer.setDataSource(audioLocalPath);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
            onProgressChange(totalTime, totalTime, false);
        });
        return this;
    }

    private void setPlayable(boolean resetPlayedTime) {
        ImageView mPlayMedia = itemView.findViewById(R.id.play);
        ImageView mPauseMedia = itemView.findViewById(R.id.pause);
        SeekBar mMediaSeekBar = itemView.findViewById(R.id.media_seekbar);

        if (mPlayMedia != null) {
            mPlayMedia.setVisibility(View.VISIBLE);
        }

        if (mPauseMedia != null) {
            mPauseMedia.setVisibility(View.GONE);
        }
        if(mMediaSeekBar != null && resetPlayedTime){
            mMediaSeekBar.setProgress(0);
        }
        if(resetPlayedTime) {
            setTotalTime();
        }
    }

    private void setPausable() {
        ImageView mPlayMedia = itemView.findViewById(R.id.play);
        ImageView mPauseMedia = itemView.findViewById(R.id.pause);
        if (mPlayMedia != null) {
            mPlayMedia.setVisibility(View.GONE);
        }

        if (mPauseMedia != null) {
            mPauseMedia.setVisibility(View.VISIBLE);
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mProgressUpdateHandler = null;
        }
    }

    //seekBar events

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        mMediaPlayer.seekTo(seekBar.getProgress());

        onProgressChange(mMediaPlayer.getCurrentPosition(), totalTime, true);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    //play,pause onclick
    @Override
    public void onClick(View v) {
        RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder)v.getTag();
        //init player???
        if (v.getId() == R.id.pause) {
            pause();
        }else if (v.getId() == R.id.play){
            play();
        }
    }

    public void setTotalTime(){
        // set total time as the audio is being played
        StringBuilder playbackStr = new StringBuilder();
        if (totalTime != 0) {
            playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) totalTime), TimeUnit.MILLISECONDS.toSeconds((long) totalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) totalTime))));
        }

        TextView mRunTime = itemView.findViewById(R.id.playback_time);
        mRunTime.setText(playbackStr);
    }

    public int getTotalTime(){
        String audioLocalPath = itemView.getContext().getExternalFilesDir(null).getAbsolutePath() + File.separator
                + CommonMethod.getFileNameFromFirebase(message.audioUrl);
        Uri uri = Uri.parse(audioLocalPath);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(itemView.getContext(), uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Integer.parseInt(durationStr);
    }

}
