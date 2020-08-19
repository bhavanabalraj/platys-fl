package com.example.platys.worker.upload

import android.content.Context
import android.widget.Toast
import androidx.work.*
import com.example.platys.utils.*
import com.example.platys.worker.PlatysDataPoint
import com.example.platys.worker.bluetooth.BleDataPoint
import com.example.platys.worker.location.LocationDataPoint
import com.example.platys.worker.makeStatusNotification
import com.example.platys.worker.motionsensor.MotionSensorData
import com.example.platys.worker.wifi.WifiDataPoint
import com.example.platys.worker.wifi.WifiWorker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UploadDataWorker @Inject constructor(
    private val context: Context,
    private val params: WorkerParameters
) : Worker(context, params) {

    private lateinit var uploadDataHelper: UploadDataHelper

    override fun doWork(): Result {
        val gson = Gson()
        val map = inputData.keyValueMap
        val wifiAccessPointsList: List<WifiDataPoint> = gson.fromJson(map[WIFI_DATA_POINTS] as String, object: TypeToken<List<WifiDataPoint>>() {}.type)
        val platysDataPoint = PlatysDataPoint(wifiAccessPointsList,
            gson.fromJson(map[LOCATION_DATA_POINT] as String, LocationDataPoint::class.java),
            gson.fromJson(map[BLUETOOTH_DATA_POINT] as String, BleDataPoint::class.java),
            gson.fromJson(map[MOTION_SENSOR_DATA_POINT] as String, MotionSensorData::class.java),
            Calendar.getInstance().timeInMillis)

        uploadDataHelper = UploadDataHelper()
        uploadDataHelper.pushData(platysDataPoint, context)
        //val map = inputData.keyValueMap
        //Toast.makeText(context, "Inside Upload worker", Toast.LENGTH_SHORT).show()
        //makeStatusNotification("Inside Upload worker", context)
        makeStatusNotification("Data pushed to firebase", context)

        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val count = sharedPreferences.getInt("push counter", 0)

        if(count <= 2) {
            val workManager = WorkManager.getInstance(context.applicationContext)
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workReq = OneTimeWorkRequestBuilder<WifiWorker>()
                .setInitialDelay(10, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .addTag("OUTPUT")
                .build()

            val continuation = workManager
                .beginUniqueWork(
                    WORKER_ID,
                    ExistingWorkPolicy.REPLACE,
                    workReq
                )

            continuation.enqueue()
        }

        return Result.success()
    }
}