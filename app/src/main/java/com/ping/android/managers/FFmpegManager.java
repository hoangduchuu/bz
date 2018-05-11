package com.ping.android.managers;

import android.content.Context;

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
}
