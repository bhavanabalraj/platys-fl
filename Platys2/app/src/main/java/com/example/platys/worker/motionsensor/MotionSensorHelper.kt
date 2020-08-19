package com.example.platys.worker.motionsensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import com.example.platys.worker.SensorDataUpdateCallback
import com.example.platys.worker.motionsensor.sensordatamodels.AccelerometerData
import com.example.platys.worker.motionsensor.sensordatamodels.GravityData
import com.example.platys.worker.motionsensor.sensordatamodels.GyroscopeData
import com.example.platys.worker.motionsensor.sensordatamodels.LinearAccelerometerData
import com.example.platys.worker.motionsensor.sensordatamodels.RotationData
import kotlinx.coroutines.*

class MotionSensorHelper : SensorEventListener {

    private lateinit var motionSensorUpdateCallback: SensorDataUpdateCallback<MotionSensorData>
    private lateinit var motionSensorData: MotionSensorData
    private var sensorManager: SensorManager? = null
    private var gravitySensor: Sensor? = null
    private var linearAccSensor: Sensor? = null
    private var rotationSensor: Sensor? = null
    private var stepCountSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null

    fun startScanning(context: Context) {
        motionSensorData = MotionSensorData()

        sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        linearAccSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        stepCountSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        gravitySensor?.also { gravity ->
            sensorManager?.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        linearAccSensor?.also { gravity ->
            sensorManager?.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        rotationSensor?.also { gravity ->
            sensorManager?.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        stepCountSensor?.also { gravity ->
            sensorManager?.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        accelerometerSensor?.also { gravity ->
            sensorManager?.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        gyroscopeSensor?.also { gravity ->
            sensorManager?.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        Thread.sleep(3000)
        motionSensorUpdateCallback.onUpdate(motionSensorData)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GRAVITY -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                motionSensorData.gravityData = GravityData(x, y, z)
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                motionSensorData.linearAccelerometerData = LinearAccelerometerData(x, y, z)
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val scalar = event.values[3]
                motionSensorData.rotationData = RotationData(x, y, z)
            }
            Sensor.TYPE_STEP_COUNTER -> {
                val steps = event.values[0]
                motionSensorData.stepCount = steps
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                motionSensorData.accelerometerData = AccelerometerData(x, y, z)
            }
            Sensor.TYPE_GYROSCOPE -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                motionSensorData.gyroscopeData = GyroscopeData(x, y, z)
            }
        }
    }

    fun setOnChangeListener(callback : SensorDataUpdateCallback<MotionSensorData>) {
        motionSensorUpdateCallback = callback
    }

    fun stopScanning() {
        sensorManager?.unregisterListener(this)
    }
}