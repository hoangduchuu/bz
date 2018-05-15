package com.ping.android.managers;

import android.content.Context;

import com.ping.android.model.enums.VoiceType;
import com.ping.android.ultility.Callback;

import java.io.File;

import nl.bravobit.ffmpeg.FFcommandExecuteResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;

public class FFmpegManager {
    private static FFmpegManager instance;
    private final Context context;

    public FFmpegManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static FFmpegManager getInstance(Context context) {
        if (instance == null) {
            instance = new FFmpegManager(context);
        }
        return instance;
    }

    public void execute(String[] cmd, FFcommandExecuteResponseHandler handler) {
        FFmpeg.getInstance(context).execute(cmd, handler);
    }

    public void transform(File input, File output, VoiceType voiceType, Callback callback) {
        String command = String.format("-i %s -af %s -acodec aac %s -strict -2",
                input.getAbsolutePath(), voiceType.getFilter(), output.getAbsolutePath());
        execute(command.split(" "), new FFcommandExecuteResponseHandler() {
            @Override
            public void onSuccess(String message) {
                callback.complete(null, message);
            }

            @Override
            public void onProgress(String message) {

            }

            @Override
            public void onFailure(String message) {
                callback.complete(message, null);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }
        });
    }
}
