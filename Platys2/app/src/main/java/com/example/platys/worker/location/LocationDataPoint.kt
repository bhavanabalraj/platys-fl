package com.example.platys.worker.location

data class LocationDataPoint(
    var lat: Double = -0.0,
    var long: Double = -0.0,
    var horizontalAccuracy: Float = 0.0F,
    var verticalAccuracy: Float = 0.0F,
    var altitude: Double = 0.0,
    var bearing: Float = 0.0F,
    var bearingAccuracy: Float = 0.0F,
    var speed: Float = 0.0F,
    var speedAccuracy: Float = 0.0F,
    var timeStamp: Long = 0
)