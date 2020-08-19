@file:JvmName("ApplicationConstants")
package com.example.platys.utils


// Name of Notification Channel for verbose notifications of background work
@JvmField val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
    "Verbose WorkManager Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications whenever work starts"
@JvmField val NOTIFICATION_TITLE: CharSequence = "WorkRequest Starting"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 1
const val WORKER_ID = "Platys Worker"

const val REQUEST_CHECK_SETTINGS = 100

const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000

const val WIFI_DATA_POINTS = "wifi-access-points"
const val LOCATION_DATA_POINT = "location-data-point"
const val BLUETOOTH_DATA_POINT = "bluetooth-data-point"
const val MOTION_SENSOR_DATA_POINT = "motion-sensor-data-point"
const val TIMESTAMP = "timestamp"


const val SHARED_PREFERENCES_NAME = "platys-shared-preferences"
const val USER_ID_SHARED_PREFERENCES_KEY = "platys-shared-preferences-user-id"
