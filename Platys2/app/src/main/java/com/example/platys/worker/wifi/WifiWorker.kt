package com.example.platys.worker.wifi

import android.content.Context
import androidx.work.*
import com.example.platys.utils.WIFI_DATA_POINTS
import com.example.platys.worker.SensorDataUpdateCallback
import com.example.platys.worker.location.LocationWorker
import com.example.platys.worker.WorkerInterface
import com.example.platys.worker.makeStatusNotification
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.util.concurrent.CountDownLatch

class WifiWorker constructor(
    private val context: Context,
    private val params: WorkerParameters
) : Worker(context, params), WorkerInterface<List<WifiDataPoint>> {

    private lateinit var latchOnWifiWorker: CountDownLatch
    private lateinit var wifiScanScanHelper: WifiScanHelper

    override fun doWork(): Result {
        //Toast.makeText(context, "Inside Wifi worker", Toast.LENGTH_SHORT).show()
        makeStatusNotification("Inside Wifi worker", context)
        wifiScanScanHelper = WifiScanHelper()

        val onChangeListener = object : SensorDataUpdateCallback<List<WifiDataPoint>> {
            override fun onUpdate(data: List<WifiDataPoint>) {
                sendDataAndStartNextWorker(data)
            }
        }

        wifiScanScanHelper.setOnChangeListener(onChangeListener)
        latchOnWifiWorker = CountDownLatch(1)
        wifiScanScanHelper.startScanning(context)

        startLatch()
        cleanUp()
        return Result.success()
    }

    override fun releaseLatch() {
        if(latchOnWifiWorker != null) {
            Timber.i("Wifi data points collected.")
            latchOnWifiWorker.countDown()
        }
    }

    override fun startLatch() {
        try {
            latchOnWifiWorker.await()
        } catch (e: InterruptedException) {
            Timber.d(e)
        }
    }

    override fun sendDataAndStartNextWorker(data: List<WifiDataPoint>) {
        Timber.i("Received wifi results. Proceeding to next worker.")
        releaseLatch()

        val constraints: Constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

//        //Parse our location to Data to use it as input for our worker
//        val tempMap: MutableMap<String, String> = mutableMapOf()
//        tempMap[WIFI_DATA_POINTS] = gson.toJson(data)

        val inputData: Data = Data.Builder()
            .putString(WIFI_DATA_POINTS, Gson().toJson(data, object: TypeToken<List<WifiDataPoint>>() {}.type))
            .build()

        //worker itself
        val locationWorker = OneTimeWorkRequest.Builder(LocationWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(locationWorker)
    }

    override fun cleanUp() {
        Timber.i("Wifi access points received. Cleaning up.")
        wifiScanScanHelper.stopScanning(context)
    }

}