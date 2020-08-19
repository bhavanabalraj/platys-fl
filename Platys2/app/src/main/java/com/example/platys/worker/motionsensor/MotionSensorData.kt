package com.example.platys.worker.motionsensor

import com.example.platys.worker.motionsensor.sensordatamodels.*

data class MotionSensorData(
    var accelerometerData: AccelerometerData = AccelerometerData(),
    var gravityData: GravityData= GravityData(),
    var linearAccelerometerData: LinearAccelerometerData = LinearAccelerometerData(),
    var rotationData: RotationData = RotationData(),
    var gyroscopeData: GyroscopeData = GyroscopeData(),
    var stepCount: Float = 0F
)