package com.example.platys.worker.location

import android.content.Context
import android.os.HandlerThread
import android.os.Looper
import android.widget.Toast
import androidx.work.*
import com.example.platys.utils.*
import com.example.platys.worker.SensorDataUpdateCallback
import com.example.platys.worker.WorkerInterface
import com.example.platys.worker.bluetooth.BluetoothWorker
import com.example.platys.worker.makeStatusNotification
import com.google.gson.Gson
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

class LocationWorker constructor(
    private val context: Context,
    private val params: WorkerParameters
) : Worker(context, params),
    WorkerInterface<LocationDataPoint> {

    private lateinit var handlerThread: HandlerThread
    private lateinit var looper: Looper
    private lateinit var locationTrackerHelper: LocationTrackerHelper
    private lateinit var latchOnLocationTracker: CountDownLatch

    override fun doWork(): Result {
        makeStatusNotification("Inside Location worker", context)
        //Toast.makeText(context, "Inside Location worker", Toast.LENGTH_SHORT).show()

        Timber.i("Location Work started")

        handlerThread = HandlerThread("LocationHandlerThread")
        handlerThread.start()
        looper = handlerThread.looper
        locationTrackerHelper =
            LocationTrackerHelper()

        val onChangeListener = object :
            SensorDataUpdateCallback<LocationDataPoint> {
            override fun onUpdate(data: LocationDataPoint) {
                sendDataAndStartNextWorker(data)
            }
        }

        locationTrackerHelper.setOnChangeListener(onChangeListener)
        latchOnLocationTracker = CountDownLatch(1)
        locationTrackerHelper.startReceivingLocationUpdate(context, looper)

        startLatch()
        cleanUp()
        return Result.success()
    }

    override fun releaseLatch() {
        if(latchOnLocationTracker != null) {
            Timber.i("Wifi data points collected.")
            latchOnLocationTracker.countDown()
        }
    }

    override fun startLatch() {
        try {
            latchOnLocationTracker.await()
        } catch (e: InterruptedException) {
            Timber.d(e)
        }
    }

    override fun cleanUp() {
        Timber.i("Location update received. Cleaning up.")
        locationTrackerHelper.stopReceivingLocationUpdates()
        looper.quit()
        handlerThread.quit()
    }

    override fun sendDataAndStartNextWorker(data: LocationDataPoint) {
        Timber.i("Received a location update. Proceeding to next worker.")
        releaseLatch()

        val constraints: Constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

//        val tempMap: MutableMap<String, String> = inputData.keyValueMap
//        val gson =
//        tempMap[LOCATION_DATA_POINT] = gson.toJson(data)

        //Parse our location to Data to use it as input for our worker
        val inputData: Data = Data.Builder()
            .putAll(inputData)
            .putString(LOCATION_DATA_POINT, Gson().toJson(data))
            .build()

        //worker itself
        val bluetoothWorker =
            OneTimeWorkRequest.Builder(BluetoothWorker::class.java).setConstraints(constraints)
                .setInputData(inputData).build()

        WorkManager.getInstance(context).enqueue(bluetoothWorker)
    }
}