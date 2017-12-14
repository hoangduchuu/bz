package com.ping.android.cameraview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.otaliastudios.cameraview.AspectRatio;
import com.otaliastudios.cameraview.CameraUtils;
import com.ping.android.activity.R;

import java.io.FileOutputStream;
import java.lang.ref.WeakReference;


public class PicturePreviewActivity extends Activity implements View.OnClickListener {

    private static WeakReference<byte[]> image;

    public static void setImage(@Nullable byte[] im) {
        image = im != null ? new WeakReference<>(im) : null;
    }

    private String outputFile;
    private int maxWidth;
    private int maxHeight;
    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preview);
        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_retry).setOnClickListener(this);

        outputFile = getIntent().getStringExtra(CameraActivity.EXTRA_OUTPUT);
        maxWidth = getIntent().getIntExtra(CameraActivity.EXTRA_MAX_WIDTH, 1000);
        maxHeight = getIntent().getIntExtra(CameraActivity.EXTRA_MAX_HEIGHT, 1000);

        final ImageView imageView = findViewById(R.id.image);

        final long delay = getIntent().getLongExtra("delay", 0);
        final int nativeWidth = getIntent().getIntExtra("nativeWidth", 0);
        final int nativeHeight = getIntent().getIntExtra("nativeHeight", 0);
        byte[] b = image == null ? null : image.get();
        if (b == null) {
            finish();
            return;
        }

        CameraUtils.decodeBitmap(b, maxWidth, maxHeight, bitmap ->  {
            this.bitmap = bitmap;
            imageView.setImageBitmap(bitmap);
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_ok:
                handleOkPress();
                break;
            case R.id.btn_retry:
                finish();
                break;
        }
    }

    private void handleOkPress() {
        if (bitmap != null) {
            saveImage(outputFile, bitmap);
            Intent intent = new Intent();
            intent.putExtra("data", outputFile);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void saveImage(String filePath, Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e1) {
            }
        }
    }
}
