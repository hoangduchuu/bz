package com.bzzzchat.videorecorder.view.facerecognition

import android.app.Activity
import android.graphics.Bitmap
import android.media.Image
import com.bzzzchat.videorecorder.view.facerecognition.mlkit.common.FrameMetadata
import com.bzzzchat.videorecorder.view.facerecognition.mlkit.common.GraphicOverlay
import com.bzzzchat.videorecorder.view.facerecognition.mlkit.common.VisionImageProcessor
import com.google.firebase.ml.common.FirebaseMLException
import java.nio.ByteBuffer

/** Custom Image Classifier Demo.  */
class CustomImageClassifierProcessor @Throws(FirebaseMLException::class)
constructor(private val activity: Activity): VisionImageProcessor {

    private val classifier: CustomImageClassifier

    init{
        classifier = CustomImageClassifier(activity)
    }

    @Throws(FirebaseMLException::class)
    override fun process(data: ByteBuffer, frameMetadata: FrameMetadata, graphicOverlay: GraphicOverlay) {
        classifier
                .classifyFrame(data, frameMetadata.width, frameMetadata.height)
                .addOnSuccessListener(
                        activity
                ) { result ->
//                    val labelGraphic = LabelGraphic(graphicOverlay)
//                    graphicOverlay.clear()
//                    graphicOverlay.add(labelGraphic)
//                    labelGraphic.updateLabel(result)
                }
    }

    override fun process(bitmap: Bitmap, graphicOverlay: GraphicOverlay) {
        // nop
    }

    override fun process(bitmap: Image, rotation:Int, graphicOverlay: GraphicOverlay) {
        // nop

    }

    override fun stop() {}
}
