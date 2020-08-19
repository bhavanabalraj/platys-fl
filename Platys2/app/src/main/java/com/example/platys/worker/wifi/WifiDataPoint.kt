package com.example.platys.worker.wifi

data class WifiDataPoint (
    val ssid: String,
    val bssid: String,
    val signalLevel: Int,
    val channelWidth: Int,
    val timestamp: Long,
    val venueName: String
)