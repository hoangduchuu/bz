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
import android.graphics.PointF;
import android.media.FaceDetector;

import com.bzzzchat.videorecorder.view.facerecognition.preprocessor.BrightnessCorrection.GammaCorrection;
import com.bzzzchat.videorecorder.view.facerecognition.preprocessor.Contours.DifferenceOfGaussian;
import com.bzzzchat.videorecorder.view.facerecognition.preprocessor.Contours.Masking;
import com.bzzzchat.videorecorder.view.facerecognition.preprocessor.ContrastAdjustment.HistogrammEqualization;
import com.bzzzchat.videorecorder.view.facerecognition.preprocessor.StandardPreprocessing.GrayScale;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.List;

public class PreProcessor {
    private Context context;
    private int angle;
    private Mat img;
    private List<Mat> images;
    private Rect[] faces;

    public PreProcessor(List<Mat> images, Context context) {
        this.images = images;
        this.context = context;
    }

    public Context getContext(){
        return context;
    }

    public void setFaces(Rect[] faces){
        this.faces = faces;
    }


    public Rect[] getFaces() {
        return faces;
    }

    public int getAngle() { return angle; }

    public void setAngle(int angle) {
        this.angle = angle;
        for (Mat img : images) {
        }
    }

    public Mat getImg() {
        return img;
    }

    public void setImages(List<Mat> images) {
        this.images = images;
    }

    public List<Mat> getImages() {
        return images;
    }

    public void setImg(Mat img) {
        this.img = img;
    }

    public void normalize0255(Mat norm){
        Core.normalize(norm, norm, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);
    }

    public PreProcessor applyGrayScale() {
        return new GrayScale().preprocessImage(this);
    }

    public PreProcessor applyGamma(double gamma) {
        return new GammaCorrection(gamma).preprocessImage(this);
    }

    public PreProcessor applyHistogramm() {
        return new HistogrammEqualization().preprocessImage(this);
    }

    public PreProcessor applyDifferenceOfGaussian(double[] sigmas) {
        return new DifferenceOfGaussian(sigmas).preprocessImage(this);
    }

    public PreProcessor applyMasking() {
        return new Masking().preprocessImage(this);
    }
}
