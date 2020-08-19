package com.example.platys.worker.bluetooth

data class BleDataPoint(
    val deviceName: String = "",
    val deviceMAC: String = "",
    val deviceType: Int = -1,
    val deviceClassType: Int? = -1,
    val bondState: Int = -1,
    val pairedDevices: List<BleDataPoint>? = null
)