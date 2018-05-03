package com.ping.android.managers;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class FFmpegManager {

    public void loadBinary(Context context) {
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        try {
            fFmpeg.loadBinary(new LoadBinaryResponseHandler() {});
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }
}
