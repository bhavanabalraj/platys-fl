package com.example.platys.worker

interface WorkerInterface<T> {
    fun startLatch()
    fun releaseLatch()
    fun sendDataAndStartNextWorker(data: T)
    fun cleanUp()
}