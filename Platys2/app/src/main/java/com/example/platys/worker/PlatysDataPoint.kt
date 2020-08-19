package com.example.platys.worker

import com.example.platys.utils.*
import com.example.platys.worker.bluetooth.BleDataPoint
import com.example.platys.worker.location.LocationDataPoint
import com.example.platys.worker.motionsensor.MotionSensorData
import com.example.platys.worker.wifi.WifiDataPoint
import com.google.firebase.database.Exclude

data class PlatysDataPoint(
    val wifiDataPoint: List<WifiDataPoint>,
    val locationDataPoint: LocationDataPoint,
    val bleDataPoint: BleDataPoint,
    val motionSensorData: MotionSensorData,
    val timeStamp: Long
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            WIFI_DATA_POINTS to wifiDataPoint,
            LOCATION_DATA_POINT to locationDataPoint,
            BLUETOOTH_DATA_POINT to bleDataPoint,
            MOTION_SENSOR_DATA_POINT to motionSensorData,
            TIMESTAMP to timeStamp
        )
    }
}