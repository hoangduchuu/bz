package com.ping.android.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

import android.os.Parcel
import android.os.Parcelable

enum class State {
    ACTIVE, INACTIVE;

    companion object {
        @JvmStatic
        fun from(value: Int): State = if (value == 0) ACTIVE else INACTIVE
    }
}

data class ExtraInfo(
        var x: Float = 0.0f,
        var y: Float = 0.0f,
        var z: Float = 0.0f,
        var state: State =  State.ACTIVE
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readFloat(),
            parcel.readFloat(),
            parcel.readFloat(),
            State.from(parcel.readInt()))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x)
        parcel.writeFloat(y)
        parcel.writeFloat(z)
        parcel.writeInt(state.ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExtraInfo> {
        override fun createFromParcel(parcel: Parcel): ExtraInfo {
            return ExtraInfo(parcel)
        }

        override fun newArray(size: Int): Array<ExtraInfo?> {
            return arrayOfNulls(size)
        }
    }

}

interface MotionCallback {
    fun onExtraInfo(extraInfo: ExtraInfo)
    fun onTable()
    fun pickedUp()
}

class MotionDetector(context: Context, val callback: MotionCallback) : SensorEventListener {
    private val mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastTimeInTable: Long = -1
    private var lastTimePickedUp: Long = -1

    fun registerMotionListener() {
        unRegisterListener()
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unRegisterListener() {
        mSensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val values = event.values
            // Movement
            val x = values[0]
            val y = values[1]
            val z = values[2]
            val extraInfo = ExtraInfo(x, y, z)
            callback.onExtraInfo(extraInfo)
            if (x in -1.0..2.0
                    && y in -1.0..2.0
                    && z in 8.0..9.9) {
                onTable()
            } else {
                pickPhoneUp()
            }
        }
    }

    private fun onTable() {
        Log.d("HEHEHE", "onTable")
        lastTimePickedUp = -1
        if (lastTimeInTable == -1L) {
            lastTimeInTable = System.currentTimeMillis()
            return
        }

        val diff = System.currentTimeMillis() - lastTimeInTable
        if (diff >= 2000) {
            callback.onTable()
        }
    }

    private fun pickPhoneUp() {
        Log.d("HEHEHE", "onPickup")
        lastTimeInTable = -1
        if (lastTimePickedUp == -1L) {
            lastTimePickedUp = System.currentTimeMillis()
            return
        }

        val diff = System.currentTimeMillis() - lastTimePickedUp
        if (diff >= 2000) {
            callback.pickedUp()
        }
    }
}