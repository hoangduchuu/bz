package com.ping.android.device.hiddenCameraEvent

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import com.ping.android.utils.bus.BusProvider


class PhoneDegreeManager(var context: Context, var activity: Activity, var busProvider: BusProvider,var listener: HiddenCameraListener) : SensorEventListener {
    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var sensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val TAG = "PhoneDegreeManager"
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var mGravity: FloatArray? = null
    private var mGeomagnetic: FloatArray? = null
    private var isStart = false
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // not using now,
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.values == null) {
            Log.e(TAG, "event.values is null")
            return
        }
        val sensorType = event.sensor.type
        when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> mGravity = event.values
            Sensor.TYPE_MAGNETIC_FIELD -> mGeomagnetic = event.values
            else -> {
                Log.e(TAG, "Unknown sensor type $sensorType")
                return
            }
        }
        if (mGravity == null) {
            Log.e(TAG, "mGravity is null")
            return
        }
        if (mGeomagnetic == null) {
            Log.e(TAG, "mGeomagnetic is null")
            return
        }
        val R = FloatArray(9)
        if (!SensorManager.getRotationMatrix(R, null, mGravity, mGeomagnetic)) {
            Log.e(TAG, "getRotationMatrix() failed")
            return
        }
        val orientation = FloatArray(9)
        SensorManager.getOrientation(R, orientation)
        val roll = orientation[1]
        val rollDeg = Math.round(Math.toDegrees(roll.toDouble())).toInt()
        val power = caculateDegrees(rollDeg)
        if (power < 40) {
            if (!isStart) {
                listener.handleStartCamera()
                isStart= true
            }
        } else {
            if (isStart) {
                listener.handleStopCamera()
                isStart = false
            }
        }
    }

    fun setupSensor() {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Detect the window position
        when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> Log.e(TAG, "Rotation 0")
            Surface.ROTATION_90 -> Log.e(TAG, "Rotation 90")
            Surface.ROTATION_180 -> Log.e(TAG, "Rotation 180")
            Surface.ROTATION_270 -> Log.e(TAG, "Rotation 270")
            else -> Log.w(TAG, "Rotation unknown")
        }
        Log.e("Start setup", "")
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            Log.e(TAG, "accelerometer is null")
        }
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magnetometer == null) {
            Log.e(TAG, "magnetometer is null")
        }
        // Detect the window position
        when (this.activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> Log.e(TAG, "Rotation 0")
            Surface.ROTATION_90 -> Log.e(TAG, "Rotation 90")
            Surface.ROTATION_180 -> Log.e(TAG, "Rotation 180")
            Surface.ROTATION_270 -> Log.e(TAG, "Rotation 270")
            else -> Log.w(TAG, "Rotation unknown")
        }
    }

    /**
     * Convert degrees to absolute tilt value between 0-100
     */
    private fun caculateDegrees(degrees: Int): Int {
        var degrees = degrees
        // Tilted back towards user more than -90 deg
        if (degrees < -90) {
            degrees = -90
        } else if (degrees > 0) {
            degrees = 0
        }// Tilted forward past 0 deg
        // Normalize into a positive value
        degrees *= -1
        // Invert from 90-0 to 0-90
        degrees = 90 - degrees
        // Convert to scale of 0-100
        val degFloat = degrees / 90f * 100f
        return degFloat.toInt()
    }


    fun onResume() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun onPause() {
        sensorManager.unregisterListener(this)
    }

    fun register() {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }
}