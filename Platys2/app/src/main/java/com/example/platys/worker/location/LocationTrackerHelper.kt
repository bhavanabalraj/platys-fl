package com.example.platys.worker.location

import android.content.Context
import android.os.Looper
import com.example.platys.utils.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
import com.example.platys.utils.UPDATE_INTERVAL_IN_MILLISECONDS
import com.example.platys.worker.SensorDataUpdateCallback
import com.google.android.gms.location.*
import timber.log.Timber

class LocationTrackerHelper {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null

    private var locationUpdateCallback: SensorDataUpdateCallback<LocationDataPoint>? = null

    init {
        locationRequest = LocationRequest()
        locationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult) // why? this. is. retarded. Android.
                val currentLocation = locationResult.lastLocation
                Timber.i("Location Callback results: $currentLocation")
                if(android.os.Build.VERSION.SDK_INT < 26) {
                    locationUpdateCallback?.onUpdate(
                        LocationDataPoint(
                            currentLocation.latitude,
                            currentLocation.longitude,
                            currentLocation.accuracy,
                            0.0F,
                            currentLocation.altitude,
                            currentLocation.bearing,
                            0.0F,
                            currentLocation.speed,
                            0.0F,
                            currentLocation.time
                        )
                    )
                } else {
                    locationUpdateCallback?.onUpdate(
                        LocationDataPoint(
                            currentLocation.latitude,
                            currentLocation.longitude,
                            currentLocation.accuracy,
                            currentLocation.verticalAccuracyMeters,
                            currentLocation.altitude,
                            currentLocation.bearing,
                            currentLocation.bearingAccuracyDegrees,
                            currentLocation.speed,
                            currentLocation.speedAccuracyMetersPerSecond,
                            currentLocation.time
                        )
                    )
                }
            }
        }
    }

    fun setOnChangeListener(callback : SensorDataUpdateCallback<LocationDataPoint>) {
        locationUpdateCallback = callback
    }

    fun startReceivingLocationUpdate(applicationContext: Context, looper: Looper) {
        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)
        try {
            mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, looper)
        } catch (e: SecurityException) {
            Timber.d(e, "Location update not possible. Required permission ot satisfied")
            locationUpdateCallback?.onUpdate(LocationDataPoint())
        }
    }

    fun stopReceivingLocationUpdates() {
        mFusedLocationClient!!.removeLocationUpdates(locationCallback)
    }
}