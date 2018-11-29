package com.ping.android.device.hiddenCameraEvent

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.ping.android.BuildConfig
import com.ping.android.utils.bus.BusProvider
import java.util.concurrent.atomic.AtomicBoolean

/**
 * https://github.com/junjunguo/android/tree/master/RotationSensor
 */

class PhoneDegreeManager(var context: Context, var activity: Activity, var busProvider: BusProvider,var listener: HiddenCameraListener) : SensorEventListener {

    private var mSensorManager: SensorManager? = null
    private var mSensorAccelerator: Sensor? = null
    private var isStart = AtomicBoolean(false)

    val TAG = "PhoneDegreeManager"

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // not using now,
    }

    override fun onSensorChanged(event: SensorEvent?) {

        val y = event!!.values[1]
        val z = event.values[2]

        val ay: Float
        val az: Float
        val angleyz: Float
        ay = y
        az = z


        angleyz = (Math.atan2(ay.toDouble(), az.toDouble()) / (Math.PI / 180)).toFloat()


        if (angleyz > BuildConfig.DEGREES_TO_START_HIDDEN_CAMERA) {
            if (!isStart.get()) {
                listener.handleStartCamera()
                isStart.set(true)
            }
        } else {
            if (isStart.get()) {
                listener.handleStopCamera()
                isStart.set(false)
            }
        }
    }

    fun setupSensor() {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensorAccelerator = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    fun register() {
        mSensorManager?.registerListener(this, mSensorAccelerator, SensorManager.SENSOR_DELAY_NORMAL);

    }

    fun unregister() {
        mSensorManager?.unregisterListener(this);
    }
}