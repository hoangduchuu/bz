package com.ping.android.presentation.view.flexibleitem.messages;

import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.transition.TransitionManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.R;
import com.ping.android.model.Message;
import com.ping.android.presentation.view.adapter.ChatMessageAdapter;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.utils.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by tuanluong on 3/2/18.
 */

public abstract class AudioMessageBaseItem extends MessageBaseItem<AudioMessageBaseItem.ViewHolder> {

    private int audioDuration = 0;
    private int currentPosition = 0;
    private AudioStatus audioStatus = AudioStatus.UNKNOWN;

    public AudioMessageBaseItem(Message message) {
        super(message);
    }

    public void completePlaying() {
        currentPosition = 0;
        audioStatus = AudioStatus.INITIALIZED;
//        if (messageListener != null) {
//            messageListener.onCompletePlayAudio(this);
//        }

    }

    public void stopSelf() {
        if (ChatMessageAdapter.currentPlayingMessage == this) {
            if (ChatMessageAdapter.audioPlayerInstance != null) {
                ChatMessageAdapter.audioPlayerInstance.pause();
            }
            if (audioStatus == AudioStatus.PLAYING) {
                audioStatus = AudioStatus.PAUSED;
            }
        }
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), parent, false);
        return new ViewHolder(view);
    }

    void setAudioDuration(int audioDuration) {
        this.audioDuration = audioDuration;
    }

    void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    int getAudioDuration() {
        return audioDuration;
    }

    int getCurrentPosition() {
        return currentPosition;
    }

    synchronized void setAudioStatus(AudioStatus audioStatus) {
        this.audioStatus = audioStatus;
    }

    synchronized AudioStatus getAudioStatus() {
        return audioStatus;
    }

    public static class ViewHolder extends MessageBaseItem.ViewHolder {
        private static final int AUDIO_PROGRESS_UPDATE_TIME = 100;

        private FirebaseStorage storage;
        private LinearLayout container;
        private TextView duration;
        private SeekBar seekBar;
        private ImageView btnPlay;
        private ImageView btnPause;
        private ImageView btnError;
        private MediaPlayer mMediaPlayer;
        private ProgressBar loadingAudioPreparing;
        private Handler mProgressUpdateHandler;
        private int totalTime;
        private AudioStatus audioStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            storage = FirebaseStorage.getInstance();
            container = itemView.findViewById(R.id.item_chat_audio);
            duration = itemView.findViewById(R.id.playback_time);
            seekBar = itemView.findViewById(R.id.media_seekbar);
            loadingAudioPreparing = itemView.findViewById(R.id.loading_audio_preparing);
            btnPlay = itemView.findViewById(R.id.play);
            btnPause = itemView.findViewById(R.id.pause);
            btnError = itemView.findViewById(R.id.img_error);
            btnPlay.setOnClickListener(this);
            btnPause.setOnClickListener(this);
            mProgressUpdateHandler = new Handler(Looper.getMainLooper());
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
            AudioMessageBaseItem audioItem = (AudioMessageBaseItem) item;
            audioStatus = audioItem.getAudioStatus();
            mMediaPlayer = null;
            switch (audioStatus) {
                case UNKNOWN:
                    setAudioSrc(item.message);
                    break;
                default:
                    initPlayer(audioStatus);
                    break;
            }
        }

        @Override
        public View getSlideView() {
            return container;
        }

        private Runnable mUpdateProgress = new Runnable() {

            public void run() {
                try {
                    if (mProgressUpdateHandler != null && audioStatus == AudioStatus.PLAYING && mMediaPlayer != null) {
                        onProgressChange(mMediaPlayer.getCurrentPosition(), totalTime, false);

                        // repeat the process
                        mProgressUpdateHandler.postDelayed(this, AUDIO_PROGRESS_UPDATE_TIME);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        private void play() {
            if (ChatMessageAdapter.currentPlayingMessage != item) {
                if (ChatMessageAdapter.currentPlayingMessage != null) {
                    ChatMessageAdapter.currentPlayingMessage.stopSelf();
                    if (messageListener != null) {
                        messageListener.onPauseAudioMessage(ChatMessageAdapter.currentPlayingMessage);
                    }
                }
                ChatMessageAdapter.currentPlayingMessage = (AudioMessageBaseItem) item;
            }
            if (mMediaPlayer == null) {
                showLoading();
                initMediaPlayer(mediaPlayer -> {
                    if (mediaPlayer == null) {
                        showError();
                        return;
                    }
                    int currentPosition = ((AudioMessageBaseItem) item).getCurrentPosition();
                    mediaPlayer.seekTo(currentPosition);
                    if (seekBar != null) {
                        seekBar.setMax(mMediaPlayer.getDuration());
                        seekBar.setProgress(currentPosition);
                    }
                    showPause();
                    showSeekbar(true);
                    mProgressUpdateHandler.postDelayed(() -> {
                        mProgressUpdateHandler.postDelayed(mUpdateProgress, AUDIO_PROGRESS_UPDATE_TIME);
                        mMediaPlayer.start();
                    }, 300);
                });
            } else {
                showPause();
                showSeekbar(false);
                mProgressUpdateHandler.postDelayed(mUpdateProgress, AUDIO_PROGRESS_UPDATE_TIME);
                int position = ((AudioMessageBaseItem) item).getCurrentPosition();
                mMediaPlayer.seekTo(position);
                if (ChatMessageAdapter.currentPlayingMessage != item) {
                    if (messageListener != null) {
                        messageListener.onPauseAudioMessage((AudioMessageBaseItem) item);
                    }
                    ChatMessageAdapter.currentPlayingMessage = (AudioMessageBaseItem) item;
                }
                mMediaPlayer.start();
            }
            audioStatus = AudioStatus.PLAYING;
            ((AudioMessageBaseItem) item).setAudioStatus(audioStatus);
        }

        private void pause() {
            if (mMediaPlayer == null) {
                return;
            }

            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                audioStatus = AudioStatus.PAUSED;
                ((AudioMessageBaseItem) item).setAudioStatus(audioStatus);
            }
            showPlay(true);
        }

        private void setAudioSrc(Message message) {
            String audioUrl = message.audioUrl;
            if (TextUtils.isEmpty(audioUrl)) {
                //itemView.findViewById(R.id.item_chat_audio).setVisibility(View.GONE);
                // FIXME: should think about message send first then uploading file
                showError();
                return;
            }
            //itemView.findViewById(R.id.item_chat_audio).setVisibility(View.VISIBLE);
            String audioLocalName = CommonMethod.getFileNameFromFirebase(audioUrl);
            final String audioLocalPath = itemView.getContext()
                    .getExternalFilesDir(null).getAbsolutePath() + File.separator + audioLocalName;
            if (audioLocalPath.endsWith("m4a")) {
                showError();
                return;
            }
            File audioLocal = new File(audioLocalPath);
            String imageLocalFolder = audioLocal.getParent();
            CommonMethod.createFolder(imageLocalFolder);

            if (audioLocal.exists()) {
                initPlayer(AudioStatus.UNKNOWN);
            } else {
                Log.d("audioUrl = " + audioUrl);
                try {
                    StorageReference audioReference = storage.getReferenceFromUrl(audioUrl);
                    audioReference.getFile(audioLocal).addOnSuccessListener(taskSnapshot -> {
                        initPlayer(AudioStatus.UNKNOWN);
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                    });
                } catch (Exception ex) {
                    Log.e(ex);
                }
            }
        }

        private void initPlayer(AudioStatus status) {
            if (status == AudioStatus.PLAYING || status == AudioStatus.PAUSED) {
                initPlayingView();
            } else if (status == AudioStatus.COMPLETED) {
                hideSeekbar(true);
                mProgressUpdateHandler.postDelayed(() -> {
                    showPlay(false);
                    seekBar.setProgress(0);
                    if (mMediaPlayer != null) {
                        mMediaPlayer.seekTo(0);
                    }
                    setTotalTime();
                }, 300);
            } else {
                showPlay(false);
                seekBar.setVisibility(View.GONE);
                if (status == AudioStatus.UNKNOWN) {
                    totalTime = getTotalTime();
                    ((AudioMessageBaseItem) item).setAudioDuration(totalTime);
                    audioStatus = AudioStatus.INITIALIZED;
                    ((AudioMessageBaseItem) item).setAudioStatus(audioStatus);
                } else {
                    totalTime = ((AudioMessageBaseItem) item).getAudioDuration();
                }
                setTotalTime();
            }
        }

        private void initPlayingView() {
            AudioMessageBaseItem audioItem = (AudioMessageBaseItem) item;
            totalTime = audioItem.getAudioDuration();
            int position = audioItem.getCurrentPosition();
            onProgressChange(position, totalTime, false);
            if (seekBar != null) {
                seekBar.setVisibility(View.VISIBLE);
                seekBar.setMax(totalTime);
                seekBar.setProgress(position);
            }
            if (audioStatus == AudioStatus.PLAYING) {
                showPause();
                mMediaPlayer = ChatMessageAdapter.audioPlayerInstance;
                mProgressUpdateHandler.postDelayed(mUpdateProgress, AUDIO_PROGRESS_UPDATE_TIME);
            } else {
                showPlay(false);
            }
        }

        private void initMediaPlayer(MediaPlayer.OnPreparedListener listener) {
            mMediaPlayer = ChatMessageAdapter.audioPlayerInstance;
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(listener);
            String audioLocalPath = itemView.getContext().getExternalFilesDir(null).getAbsolutePath() + File.separator
                    + CommonMethod.getFileNameFromFirebase(item.message.audioUrl);
            if (audioLocalPath.endsWith("m4a")) {
                showError();
                return;
            }
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(audioLocalPath);
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
                showError();
            }
        }

        private void onProgressChange(int currentTime, int totalTime, boolean isFromSeekBar) {
            StringBuilder playbackStr = new StringBuilder();
            ((AudioMessageBaseItem) item).setCurrentPosition(currentTime);
            int remainingTime = totalTime - currentTime;
            if (mMediaPlayer != null) {
                seekBar.setProgress(currentTime);
            }

            // set the current time
            // its ok to show 00:00 in the UI
            playbackStr.append(String.format(Locale.getDefault(), "%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) remainingTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) remainingTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) remainingTime))));
            if (duration != null) {
                duration.setText(playbackStr.toString());
            }
        }

        private void setTotalTime() {
            // set total time as the audio is being played
            StringBuilder playbackStr = new StringBuilder();
            if (totalTime != 0) {
                playbackStr.append(String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) totalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) totalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) totalTime))));
            }

            duration.setText(playbackStr);
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

        private void showPlay(boolean isAnimate) {
            loadingAudioPreparing.setVisibility(View.GONE);
            btnPause.setVisibility(View.GONE);
            btnError.setVisibility(View.GONE);
            if (isAnimate) {
                TransitionManager.beginDelayedTransition((ViewGroup) itemView);
            }
            btnPlay.setVisibility(View.VISIBLE);
        }

        private void showPause() {
            loadingAudioPreparing.setVisibility(View.GONE);
            btnError.setVisibility(View.GONE);
            btnPlay.setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition((ViewGroup) itemView);
            btnPause.setVisibility(View.VISIBLE);
        }

        private void showLoading() {
            btnPause.setVisibility(View.GONE);
            btnError.setVisibility(View.GONE);
            btnPlay.setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition((ViewGroup) itemView);
            loadingAudioPreparing.setVisibility(View.VISIBLE);
        }

        private void hideLoading() {
            loadingAudioPreparing.setVisibility(View.GONE);
            btnPause.setVisibility(View.GONE);
            btnError.setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition((ViewGroup) itemView);
            btnPlay.setVisibility(View.VISIBLE);
        }

        private void showError() {
            loadingAudioPreparing.setVisibility(View.GONE);
            btnPause.setVisibility(View.GONE);
            btnPlay.setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition((ViewGroup) itemView);
            btnError.setVisibility(View.VISIBLE);
        }

        private void showSeekbar(boolean isAnimate) {
            if (isAnimate) {
                TransitionManager.beginDelayedTransition((ViewGroup) itemView);
            }
            seekBar.setVisibility(View.VISIBLE);
        }

        private void hideSeekbar(boolean isAnimate) {
            if (isAnimate) {
                TransitionManager.beginDelayedTransition((ViewGroup) itemView);
            }
            seekBar.setVisibility(View.GONE);
        }
    }

    enum AudioStatus {
        UNKNOWN,
        INITIALIZED,
        PLAYING,
        PAUSED,
        COMPLETED,
    }
}
