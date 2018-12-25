package com.ping.android.presentation.view.flexibleitem.messages;

import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.transition.TransitionManager;
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
import com.ping.android.managers.FFmpegManager;
import com.ping.android.model.Message;
import com.ping.android.model.enums.VoiceType;
import com.ping.android.presentation.view.adapter.ChatMessageAdapter;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.FileHelperKt;
import com.ping.android.utils.Log;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

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
    }

    public void stopSelf() {
        if (ChatMessageAdapter.currentPlayingMessage == this) {
            if (ChatMessageAdapter.audioPlayerInstance != null) {
                ChatMessageAdapter.audioPlayerInstance.pause();
            }
            if (audioStatus == AudioStatus.PLAYING) {
                audioStatus = AudioStatus.INITIALIZED;
                currentPosition = 0;
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
            container = itemView.findViewById(R.id.item_chat_message);
            duration = itemView.findViewById(R.id.playback_time);
            seekBar = itemView.findViewById(R.id.media_seekbar);
            loadingAudioPreparing = itemView.findViewById(R.id.loading_audio_preparing);
            btnPlay = itemView.findViewById(R.id.play);
            btnPause = itemView.findViewById(R.id.pause);
            btnError = itemView.findViewById(R.id.img_error);
            btnPlay.setOnClickListener(this);
            btnPause.setOnClickListener(this);
            mProgressUpdateHandler = new Handler(Looper.getMainLooper());

            initGestureListener();
        }

        @Override
        protected View getClickableView() {
            return container;
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
            totalTime = 0;
            setTotalTime();
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
        public void onDoubleTap() {
            if (item.isEditMode) {
                return;
            }
            pause();
            maskStatus = !maskStatus;
            if (messageListener != null) {
                messageListener.updateMessageMask(item.message, maskStatus, lastItem);
            }
        }

        @Override
        public void onLongPress() {
            if (messageListener != null) {
                messageListener.onLongPress(item);
            }
        }

        @Override
        public void onSingleTap() {

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
                String audioFile = getSuitableAudioFile(item.message.localFilePath);
                initMediaPlayer(audioFile, mediaPlayer -> {
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
            String audioLocalPath = "";
            if (!TextUtils.isEmpty(message.localFilePath)) {
                audioLocalPath = message.localFilePath;
            } else {
                String audioUrl = message.mediaUrl;
                if (TextUtils.isEmpty(audioUrl)) {
                    //itemView.findViewById(R.id.item_chat_audio).setVisibility(View.GONE);
                    // FIXME: should think about message send first then uploading file
                    showError();
                    return;
                }
                if (audioUrl.startsWith("gs://")) {
                    audioLocalPath = getLocalFilePath(audioUrl);
                } else {
                    // This is cache url in case of no internet connection
                    audioLocalPath = audioUrl;
                }
            }
            if (audioLocalPath.endsWith("m4a")) {
                showError();
                return;
            }
            File audioLocal = new File(audioLocalPath);
            String imageLocalFolder = audioLocal.getParent();
            CommonMethod.createFolder(imageLocalFolder);
            VoiceType voiceType = VoiceType.from(message.voiceType);
            item.message.localFilePath = audioLocalPath;
            if (audioLocal.exists()) {
                if (voiceType != VoiceType.DEFAULT && message.maskStatus()) {
                    prepareAudioMask(audioLocal);
                } else {
                    initPlayer(AudioStatus.UNKNOWN);
                }
            } else {
                try {
                    StorageReference audioReference = storage.getReferenceFromUrl(message.mediaUrl);
                    audioReference.getFile(audioLocal).addOnSuccessListener(taskSnapshot -> {
                        // Prepare audio file
                        if (voiceType != VoiceType.DEFAULT && message.maskStatus()) {
                            prepareAudioMask(audioLocal);
                        } else {
                            initPlayer(AudioStatus.UNKNOWN);
                        }
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                        showError();
                    });
                } catch (Exception ex) {
                    Log.e(ex);
                }
            }
        }

        private String getLocalFilePath(String audioUrl) {
            String audioLocalName = CommonMethod.getFileNameFromFirebase(audioUrl);
            return itemView.getContext()
                    .getExternalCacheDir().getAbsolutePath() + File.separator + audioLocalName;
        }

        private String getSuitableAudioFile(String audioUrl) {
            if (TextUtils.isEmpty(audioUrl)) return "";
            File localFile = new File(audioUrl);
            VoiceType voiceType = VoiceType.from(item.message.voiceType);
            if (voiceType != VoiceType.DEFAULT && item.message.maskStatus()) {
                String transformFileName = voiceType.toString() + localFile.getName();
                File transformFile = new File(localFile.getParent(), transformFileName);
                return transformFile.getAbsolutePath();
            }
            return localFile.getAbsolutePath();
        }

        private void prepareAudioMask(File audioLocal) {
            item.message.localFilePath = audioLocal.getAbsolutePath();
            VoiceType voiceType = VoiceType.from(item.message.voiceType);
            if (voiceType != VoiceType.DEFAULT) {
                String transformFileName = voiceType.toString() + audioLocal.getName();
                File transformFile = new File(audioLocal.getParent(), transformFileName);
                if (transformFile.exists()) {
                    initPlayer(AudioStatus.UNKNOWN);
                } else {
                    FFmpegManager.getInstance(itemView.getContext()).transform(audioLocal, transformFile, voiceType, (error, data) -> {
                        if (error == null) {
                            initPlayer(AudioStatus.UNKNOWN);
                        }
                    });
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
                    totalTime = getTotalTime(getSuitableAudioFile(item.message.localFilePath));
                    ((AudioMessageBaseItem) item).setAudioDuration(totalTime);
                    audioStatus = AudioStatus.INITIALIZED;
                    ((AudioMessageBaseItem) item).setAudioStatus(audioStatus);
                } else {
                    totalTime = ((AudioMessageBaseItem) item).getAudioDuration();
                }
                if (totalTime > 0) {
                    setTotalTime();
                } else {
                    showError();
                }
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

        private void initMediaPlayer(String audioFile, MediaPlayer.OnPreparedListener listener) {
            mMediaPlayer = ChatMessageAdapter.audioPlayerInstance;
            if (mMediaPlayer == null) return;
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(listener);
            if (audioFile.endsWith("m4a")) {
                showError();
                return;
            }
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(audioFile);
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
            } else {
                playbackStr.append("00:00");
            }

            duration.setText(playbackStr);
        }

        private int getTotalTime(String audioPath) {
            Uri uri = FileHelperKt.uri(new File(audioPath), itemView.getContext());
            String durationStr = "";
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(itemView.getContext(), uri);
                durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            if (!TextUtils.isEmpty(durationStr)) {
                return Integer.parseInt(durationStr);
            } else {
                return 0;
            }
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
