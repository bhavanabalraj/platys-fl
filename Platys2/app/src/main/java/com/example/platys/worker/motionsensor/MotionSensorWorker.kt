package com.example.platys.worker.motionsensor

import android.content.Context
import android.widget.Toast
import androidx.work.*
import com.example.platys.utils.LOCATION_DATA_POINT
import com.example.platys.utils.MOTION_SENSOR_DATA_POINT
import com.example.platys.worker.SensorDataUpdateCallback
import com.example.platys.worker.WorkerInterface
import com.example.platys.worker.makeStatusNotification
import com.example.platys.worker.upload.UploadDataWorker
import com.google.gson.Gson
import timber.log.Timber
import java.util.concurrent.CountDownLatch

class MotionSensorWorker constructor(
    private val context: Context,
    private val params: WorkerParameters
) : Worker(context, params), WorkerInterface<MotionSensorData> {

    private lateinit var latchOnMotionSensorWorker: CountDownLatch
    private lateinit var motionSensorHelper: MotionSensorHelper

    override fun doWork(): Result {
        //Toast.makeText(context, "Inside Motion Sensor worker", Toast.LENGTH_SHORT).show()
        makeStatusNotification("Inside Motion Sensor worker", context)

        motionSensorHelper = MotionSensorHelper()

        val onChangeListener = object : SensorDataUpdateCallback<MotionSensorData> {
            override fun onUpdate(data: MotionSensorData) {
                sendDataAndStartNextWorker(data)
            }
        }

        motionSensorHelper.setOnChangeListener(onChangeListener)
        latchOnMotionSensorWorker = CountDownLatch(1)
        motionSensorHelper.startScanning(context)
        startLatch()
        cleanUp()
        return Result.success()
    }

    override fun releaseLatch() {
        if(latchOnMotionSensorWorker != null) {
            Timber.i("Motion sensor data points collected.")
            latchOnMotionSensorWorker.countDown()
        }
    }

    override fun startLatch() {
        try {
            latchOnMotionSensorWorker.await()
        } catch (e: InterruptedException) {
            Timber.d(e)
        }
    }

    override fun sendDataAndStartNextWorker(data: MotionSensorData) {
        Timber.i("Received motion sensor data results. Proceeding to next worker.")
        releaseLatch()

        val constraints: Constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

//        val tempMap: MutableMap<String, Any> = inputData.keyValueMap
//        val gson = Gson()
//        tempMap[MOTION_SENSOR_DATA_POINT] = gson.toJson(data)

        //Parse our location to Data to use it as input for our worker
        //Parse our location to Data to use it as input for our worker
        val inputData: Data = Data.Builder()
            .putAll(inputData)
            .putString(MOTION_SENSOR_DATA_POINT, Gson().toJson(data))
            .build()

        //worker itself
        val uploadDataWorker = OneTimeWorkRequest.Builder(UploadDataWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(uploadDataWorker)
    }

    override fun cleanUp() {
        Timber.i("Motion sensor data received. Cleaning up.")
        motionSensorHelper.stopScanning()
    }
}