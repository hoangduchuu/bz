package com.ping.android.presentation.view.flexibleitem.messages;

import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.activity.R;
import com.ping.android.model.Message;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.utils.AudioMessagePlayer;
import com.ping.android.utils.Log;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by tuanluong on 3/2/18.
 */

public abstract class AudioMessageBaseItem extends MessageBaseItem<AudioMessageBaseItem.ViewHolder> {

    public AudioMessageBaseItem(Message message) {
        super(message);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends MessageBaseItem.ViewHolder {
        private static final int AUDIO_PROGRESS_UPDATE_TIME = 100;

        private FirebaseStorage storage;
        private TextView duration;
        private SeekBar seekBar;
        private ImageView btnPlay;
        private ImageView btnPause;
        private MediaPlayer mMediaPlayer;
        private Handler mProgressUpdateHandler;
        private int totalTime;

        public ViewHolder(View itemView) {
            super(itemView);
            storage = FirebaseStorage.getInstance();
            duration = itemView.findViewById(R.id.playback_time);
            seekBar = itemView.findViewById(R.id.media_seekbar);
            btnPlay = itemView.findViewById(R.id.play);
            btnPause = itemView.findViewById(R.id.pause);
            btnPlay.setOnClickListener(this);
            btnPause.setOnClickListener(this);
        }

        @Override
        protected View getClickableView() {
            return null;
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
            switch (view.getId()) {
                case R.id.play:
                    play();
                    break;
                case R.id.pause:
                    pause();
                    break;
            }
        }

        @Override
        public void bindData(MessageBaseItem item, boolean lastItem) {
            super.bindData(item, lastItem);
            setAudioSrc(item.message);
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

        private void setAudioSrc(Message message) {
            String audioUrl = message.audioUrl;
            if (TextUtils.isEmpty(audioUrl)) {
                itemView.findViewById(R.id.item_chat_audio).setVisibility(View.GONE);
                return;
            }
            itemView.findViewById(R.id.item_chat_audio).setVisibility(View.VISIBLE);
            String audioLocalName = CommonMethod.getFileNameFromFirebase(audioUrl);
            final String audioLocalPath = itemView.getContext()
                    .getExternalFilesDir(null).getAbsolutePath() + File.separator + audioLocalName;

            File audioLocal = new File(audioLocalPath);
            String imageLocalFolder = audioLocal.getParent();
            CommonMethod.createFolder(imageLocalFolder);

            if (audioLocal.exists()) {
                initPlayer(message);
            } else {
                Log.d("audioUrl = " + audioUrl);
                try {
                    StorageReference audioReference = storage.getReferenceFromUrl(audioUrl);
                    audioReference.getFile(audioLocal).addOnSuccessListener(taskSnapshot -> {
                        initPlayer(message);
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                    });
                } catch (Exception ex) {
                    Log.e(ex);
                }
            }
        }

        public void initPlayer(Message message) {
            totalTime = getTotalTime();
            setPlayable(true);

            if (mMediaPlayer != null) {
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
        }

        public void onProgressChange(int currentTime, int totalTime, boolean isFromSeekBar) {
            StringBuilder playbackStr = new StringBuilder();
            int remainingTime = totalTime - currentTime;

            // set the current time
            // its ok to show 00:00 in the UI
            playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) remainingTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) remainingTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) remainingTime))));
            if (duration != null) {
                duration.setText(playbackStr.toString());
            }
            if (!isFromSeekBar && seekBar != null) {
                seekBar.setProgress(currentTime);
            }

            if (currentTime == totalTime) {
                setPlayable(true);
            }
        }

        public void setTotalTime() {
            // set total time as the audio is being played
            StringBuilder playbackStr = new StringBuilder();
            if (totalTime != 0) {
                playbackStr.append(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) totalTime), TimeUnit.MILLISECONDS.toSeconds((long) totalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) totalTime))));
            }

            TextView mRunTime = itemView.findViewById(R.id.playback_time);
            mRunTime.setText(playbackStr);
        }

        private int getTotalTime() {
            String audioLocalPath = itemView.getContext().getExternalFilesDir(null).getAbsolutePath() + File.separator
                    + CommonMethod.getFileNameFromFirebase(item.message.audioUrl);
            Uri uri = Uri.parse(audioLocalPath);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(itemView.getContext(), uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Integer.parseInt(durationStr);
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
            if (mMediaSeekBar != null && resetPlayedTime) {
                mMediaSeekBar.setProgress(0);
            }
            if (resetPlayedTime) {
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
    }
}
