/* Copyright 2016 Michael Sladoje and Mike Schälchli. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.bzzzchat.videorecorder.view.facerecognition.preprocessor;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.osgi.OpenCVNativeLoader;

import java.util.ArrayList;
import java.util.List;


public class PreProcessorFactory {
    private Context context;
    private PreProcessor preProcessorRecognition;
    private List<Mat> images;

    static {
        new OpenCVNativeLoader().init();
    }

    public PreProcessorFactory(Context context) {
        this.context = context;
    }

    public Mat processBitmap(Bitmap bitmap) {
        Mat mat = new Mat();
        org.opencv.android.Utils.bitmapToMat(bitmap, mat);
        return getProcessedImage(mat).get(0);
    }

    public List<Mat> getProcessedImage(Mat img) throws NullPointerException {

        images = new ArrayList<Mat>();
        images.add(img);
        preProcessorRecognition = new PreProcessor(images, context);

        try {
            preProcessorRecognition
                    .applyGrayScale()
                    .applyGamma(0.2)
                    .applyHistogramm();
        } catch (NullPointerException e){
            Log.d("getProcessedImage", "No face detected");
            return null;
        }
            return preProcessorRecognition.getImages();
    }

    public Rect[] getFacesForRecognition() {
        if(preProcessorRecognition != null){
            return preProcessorRecognition.getFaces();
        } else {
            return null;
        }
    }

    private List<Mat> getCopiedImageList(Mat img){
        List<Mat> images = new ArrayList<Mat>();
        Mat imgCopy = new Mat();
        img.copyTo(imgCopy);
        images.add(imgCopy);
        return images;
    }

    public int getAngleForRecognition(){
        return preProcessorRecognition.getAngle();
    }


}
