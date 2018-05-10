package com.ping.android.managers;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class FFmpegManager {
    private static FFmpegManager instance;
    private final Context context;

    public FFmpegManager(Context context) {
        this.context = context.getApplicationContext();
        loadBinary();
    }

    public static FFmpegManager getInstance(Context context) {
        if (instance == null) {
            instance = new FFmpegManager(context);
        }
        return instance;
    }

    private void loadBinary() {
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        try {
            fFmpeg.loadBinary(new LoadBinaryResponseHandler() {});
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void execute(String[] cmd, FFmpegExecuteResponseHandler handler) {
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        try {
            if (fFmpeg.isFFmpegCommandRunning()) {
                fFmpeg.killRunningProcesses();
            }
            fFmpeg.execute(cmd, handler);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }
}
