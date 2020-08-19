package com.example.platys.worker.upload

import android.content.Context
import com.example.platys.utils.SHARED_PREFERENCES_NAME
import com.example.platys.utils.USER_ID_SHARED_PREFERENCES_KEY
import com.example.platys.worker.PlatysDataPoint
import com.example.platys.worker.SensorDataUpdateCallback
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class UploadDataHelper {

    private var updateWorkerCallback: SensorDataUpdateCallback<Boolean>? = null

    fun pushData(data: PlatysDataPoint, context: Context) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val count = sharedPreferences.getInt("push counter", 0) + 1
        sharedPreferences.edit().putInt("push counter", count).apply()

        val userID: String?  = sharedPreferences.getString(USER_ID_SHARED_PREFERENCES_KEY, "123rht67")

        val ref: DatabaseReference = Firebase.database.reference
        val key = ref.child("sensor-data").child(userID!!).push().key

        if(key == null) {
            Timber.w("Couldn't push data")
            return
        }

        val sensorDataValues = data.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "/sensor-data/$userID/$key" to sensorDataValues
        )

        ref.updateChildren(childUpdates)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    updateWorkerCallback?.onUpdate(true)
                } else {
                    updateWorkerCallback?.onUpdate(false)
                }
            }
    }

    fun setOnChangeListener(callback : SensorDataUpdateCallback<Boolean>) {
        updateWorkerCallback = callback
    }
}