package com.example.platys.worker

interface SensorDataUpdateCallback<T> {
    fun onUpdate(data: T)
}