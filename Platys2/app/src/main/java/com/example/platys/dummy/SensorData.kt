package com.example.platys.dummy

data class SensorData(
    var lat: String = "",
    var long: String = "",
    var activity: String = "",
    var blData: MutableList<BluetoothData> = mutableListOf(),
    var wifiData: MutableList<WifiData> = mutableListOf(),
    var accData: AccData = AccData(),
    var gravityData: Gravity = Gravity(),
    var linearAccData: LinearAcc = LinearAcc(),
    var rotationData: RotationData = RotationData(),
    var gyroscopeData: GyroscopeData = GyroscopeData(),
    var steps: Float = 0F
)