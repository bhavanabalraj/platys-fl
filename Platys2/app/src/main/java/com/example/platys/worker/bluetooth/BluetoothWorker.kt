package com.example.platys.worker.bluetooth

import android.content.Context
import androidx.work.*
import com.example.platys.utils.BLUETOOTH_DATA_POINT
import com.example.platys.worker.SensorDataUpdateCallback
import com.example.platys.worker.WorkerInterface
import com.example.platys.worker.makeStatusNotification
import com.example.platys.worker.motionsensor.MotionSensorWorker
import com.google.gson.Gson
import timber.log.Timber
import java.util.concurrent.CountDownLatch

class BluetoothWorker constructor(
    private val context: Context,
    private val params: WorkerParameters
) : Worker(context, params),
    WorkerInterface<BleDataPoint> {

    private lateinit var latchOnBluetoothWorker: CountDownLatch
    private lateinit var bluetoothDiscoveryHelper: BluetoothDiscoveryHelper

    override fun doWork(): Result {
        //Toast.makeText(context, "Inside Bluetooth worker", Toast.LENGTH_SHORT).show()
        makeStatusNotification("Inside Bluetooth worker", context)
        bluetoothDiscoveryHelper = BluetoothDiscoveryHelper()

        val onChangeListener = object :
            SensorDataUpdateCallback<BleDataPoint> {
            override fun onUpdate(data: BleDataPoint) {
                sendDataAndStartNextWorker(data)
            }
        }

        bluetoothDiscoveryHelper.setOnChangeListener(onChangeListener)
        latchOnBluetoothWorker = CountDownLatch(1)
        bluetoothDiscoveryHelper.startDiscovery(context)
        startLatch()
        cleanUp()
        return Result.success()
    }

    override fun releaseLatch() {
        if(latchOnBluetoothWorker != null) {
            Timber.i("Ble data points collected.")
            latchOnBluetoothWorker.countDown()
        }
    }

    override fun startLatch() {
        try {
            latchOnBluetoothWorker.await()
        } catch (e: InterruptedException) {
            Timber.d(e)
        }
    }

    override fun sendDataAndStartNextWorker(data: BleDataPoint) {
        Timber.i("Received bluetooth results. Proceeding to next worker.")
        releaseLatch()

        val constraints: Constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

//        val tempMap: MutableMap<String, Any> = inputData.keyValueMap
//        val gson = Gson()
//        tempMap[BLUETOOTH_DATA_POINT] = gson.toJson(data)

        //Parse our location to Data to use it as input for our worker
        //Parse our location to Data to use it as input for our worker
        val inputData: Data = Data.Builder()
            .putAll(inputData)
            .putString(BLUETOOTH_DATA_POINT, Gson().toJson(data))
            .build()

        //worker itself
        val motionSensorWorker = OneTimeWorkRequest.Builder(MotionSensorWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(motionSensorWorker)
    }

    override fun cleanUp() {
        Timber.i("Ble data points received. Cleaning up.")
        bluetoothDiscoveryHelper.stopDiscovery(context)
    }
}