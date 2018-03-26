package com.ping.android.presentation.view.custom;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;

/**
 * Created by tuanluong on 3/23/18.
 */

public class BzzzSurfaceView extends QBRTCSurfaceView {
    public BzzzSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BzzzSurfaceView(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //thread.setBubble(touchX, touchY);
                break;
        }
        return true;
    }

    private void setTranslucent(boolean translucent) {
        if (translucent) {
            setZOrderOnTop(true);
            //setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            this.getHolder().setFormat(PixelFormat.RGBA_8888);
        } else {
            this.getHolder().setFormat(PixelFormat.RGB_565);
        }
    }
}
