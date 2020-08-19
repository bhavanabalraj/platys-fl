package com.example.platys.worker.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import com.example.platys.worker.SensorDataUpdateCallback
import timber.log.Timber

class WifiScanHelper {
    private lateinit var wifiManager: WifiManager
    private var wifiScanReceiver: BroadcastReceiver
    private var wifiDataUpdateCallback: SensorDataUpdateCallback<List<WifiDataPoint>>? = null

    init {
        wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val scanResults = wifiManager.scanResults
                val wifiPoints = mutableListOf<WifiDataPoint>()

                for(accessPoint in scanResults) {
                    wifiPoints.add(
                        WifiDataPoint(
                            accessPoint.SSID,
                            accessPoint.BSSID,
                            accessPoint.level,
                            accessPoint.channelWidth,
                            accessPoint.timestamp,
                            accessPoint.venueName.toString()
                        )
                    )
                }

                wifiDataUpdateCallback?.onUpdate(wifiPoints.toList())
            }
        }
    }

    fun startScanning(context: Context) {
        wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        context.registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        try {
            wifiManager.startScan()
        } catch (e: SecurityException) {
            Timber.d(e)
            wifiDataUpdateCallback?.onUpdate(listOf())
        }
    }

    fun stopScanning(context: Context) {
        context.unregisterReceiver(wifiScanReceiver)
    }

    fun setOnChangeListener(callback: SensorDataUpdateCallback<List<WifiDataPoint>>) {
        wifiDataUpdateCallback = callback
    }
}