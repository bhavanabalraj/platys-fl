package com.example.platys.dummy

data class WifiData (
    val ssid: String,
    val bssid: String,
    val signalLevel: Int,
    val channelWidth: Int,
    val is5GHZ: String,
    val is24GHZ: String
)