package com.example.platys.dummy

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.example.platys.R
import com.example.platys.utils.REQUEST_CHECK_SETTINGS
import com.example.platys.utils.SHARED_PREFERENCES_NAME
import com.example.platys.utils.WORKER_ID
import com.example.platys.worker.wifi.WifiWorker
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.lang.ClassCastException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DummyActivity : AppCompatActivity(), SensorEventListener {

    private var location: Location? = null
    var lat: String? = null
    var long: String? = null
    private var wifiManager: WifiManager? = null
    private var sensorData: SensorData = SensorData()
    private val activities = listOf("Still", "Walking", "Running", "onbicycle")
    private val flags = listOf("True", "False")
    lateinit var dataBase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dummy_layout)
        val button = findViewById<MaterialButton>(R.id.button)
        val textView = findViewById<TextView>(R.id.textView)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = database.getReference("Platys/platys")
        val sensorDatRef = ref.child("sensor")

        val mLocationRequestHighAccuracy = LocationRequest()
        mLocationRequestHighAccuracy.priority = PRIORITY_HIGH_ACCURACY
        val mLocationRequestBalancedPowerAccuracy = LocationRequest()
        mLocationRequestBalancedPowerAccuracy.priority = PRIORITY_BALANCED_POWER_ACCURACY

        val builder: LocationSettingsRequest.Builder =
            LocationSettingsRequest.Builder().addLocationRequest(mLocationRequestHighAccuracy)
                .addLocationRequest(mLocationRequestBalancedPowerAccuracy)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        result.addOnCompleteListener { response ->
            try {
                val locationSettingsResponse = response.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                when(e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            val exception = e as ResolvableApiException
                            exception.startResolutionForResult(
                                this@DummyActivity,
                                REQUEST_CHECK_SETTINGS
                            )
                        } catch (ex: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (ex: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        // do nothing
                    }
                }
            }
        }

        button.setOnClickListener {
            sensorData.activity = activities[Random.nextInt(4)]
            textView.text = sensorData.toString()

            val workManager = WorkManager.getInstance(application)
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workReq = OneTimeWorkRequestBuilder<WifiWorker>()
                .setInitialDelay(10, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .addTag("OUTPUT")
                .build()

            val continuation = workManager
                .beginUniqueWork(
                    WORKER_ID,
                    ExistingWorkPolicy.REPLACE,
                    workReq
                )

            continuation.enqueue()

//            sensorDatRef.push().setValue(sensorData)
//                .addOnSuccessListener {
//                    Log.d("DummyActivity", "loadPostWorked")
//                }
//                .addOnFailureListener {
//                    // Write failed
//                    // ...
//                    Log.w("DummyActivity", "loadPost:onCancelled", it)
//                }

//            Firebase.auth.signInWithEmailAndPassword("abc@abc.com", "abc123")
//                .addOnCompleteListener { task ->
//                    if(task.isSuccessful) {
//                        Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show()
//                        dataBase.child("data").setValue(sensorData)
//                    } else {
//                    }
//                }
        }

        location = Location(this, object : LocationListener {
            override fun locationResponse(locationResult: LocationResult) {
                lat = locationResult.lastLocation.latitude.toString()
                long = locationResult.lastLocation.longitude.toString()
                sensorData.lat = lat as String
                sensorData.long = long as String
                //Toast.makeText(this@MainActivity, "Lat:$lat Long:$long", Toast.LENGTH_SHORT).show()
            }
        })

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

        when {
            bluetoothAdapter == null -> {
                Toast.makeText(this, "Device does not support bluetooth", Toast.LENGTH_SHORT).show()
            }
            bluetoothAdapter.isEnabled -> {
                //Toast.makeText(activity, "Bluetooth is switched on", Toast.LENGTH_SHORT).show()
            }
            else -> {
                //Toast.makeText(activity, "Bluetooth is switched off", Toast.LENGTH_SHORT).show()
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 10)
            }
        }

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val gravitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val linearAccSensor: Sensor? =
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val stepCountSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val accSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        gravitySensor?.also { gravity ->
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        linearAccSensor?.also { gravity ->
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        rotationSensor?.also { gravity ->
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        stepCountSensor?.also { gravity ->
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        accSensor?.also { gravity ->
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        gySensor?.also { gravity ->
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }

//        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
//            102)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        this.registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))

        val success = wifiManager!!.startScan()
        if (!success) {
            // scan failure handling
            scanFailure()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 102
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Do something with granted permission
            scanSuccess()
        } else {
            location?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onStart() {
        super.onStart()
        location?.initializeLocation()
    }

    override fun onPause() {
        super.onPause()
        location?.stopUpdateLocation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            10 -> {
                if (resultCode == Activity.RESULT_OK) {
                    turnBluetooth()
                } else {
                    Toast.makeText(
                        this,
                        "You did not give permissions to access bluetooth",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            REQUEST_CHECK_SETTINGS -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {

                    }
                    Activity.RESULT_CANCELED -> {

                    }
                    else -> {}
                }
            }
            else -> {
            }
        }
    }

    private val btStateChangeListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val newState = intent?.extras?.get("EXTRA_STATE")
            val oldState = intent?.extras?.get("EXTRA_PREVIOUS_STATE")

            if (newState == BluetoothAdapter.STATE_ON && (oldState == BluetoothAdapter.STATE_OFF || oldState == BluetoothAdapter.STATE_TURNING_OFF)) {
                turnBluetooth()
            } else if (newState == BluetoothAdapter.STATE_OFF && (oldState == BluetoothAdapter.STATE_ON || oldState == BluetoothAdapter.STATE_TURNING_ON)) {
                Toast.makeText(
                    this@DummyActivity,
                    "You turned off bluetooth. This application needs bluetooth to be switched on",
                    Toast.LENGTH_SHORT
                ).show()
                unregisterReceivers()
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val btReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device!!.name
                    val deviceHardwareAddress = device!!.address // MAC address
                    sensorData.blData.add(BluetoothData(deviceName, deviceHardwareAddress))
                }
            }
        }
    }

    private fun scanSuccess() {
        val results = wifiManager!!.scanResults
        for(accesspoint in results) {
            val index = Random.nextInt(2)
            val flag1 = flags[index]
            var flag2 = "False"
            if(index == 1) {
                flag2 = "True"
            }

            sensorData.wifiData.add(WifiData(accesspoint.SSID, accesspoint.BSSID, accesspoint.level,
                accesspoint.channelWidth, flag1, flag2));
        }
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        //val results = wifiManager.scanResults
        if (wifiManager!!.isWifiEnabled) {
            Toast.makeText(this, "Wifi scan failed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please turn on wifi", Toast.LENGTH_SHORT).show()
        }
    }

    fun turnBluetooth() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(btReceiver, filter)

        val filter1 = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(btStateChangeListener, filter1)
    }

    fun unregisterReceivers() {
        unregisterReceiver(btReceiver)
        unregisterReceiver(btStateChangeListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GRAVITY -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                sensorData.gravityData = Gravity(x, y, z)
            }
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                sensorData.linearAccData = LinearAcc(x, y, z)
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val scalar = event.values[3]
                sensorData.rotationData = RotationData(x, y, z)
            }
            Sensor.TYPE_STEP_COUNTER -> {
                val steps = event.values[0]
                sensorData.steps = steps
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                sensorData.accData = AccData(x, y, z)
            }
            Sensor.TYPE_GYROSCOPE -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                sensorData.gyroscopeData = GyroscopeData(x, y, z)
            }
        }
    }

//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) {
//            hideSystemUIAndNavigation(this)
//        }
//    }
//
//    private fun hideSystemUIAndNavigation(activity: Activity) {
//        val decorView: View = activity.window.decorView
//        decorView.systemUiVisibility =
//            (View.SYSTEM_UI_FLAG_IMMERSIVE
//                    // Set the content to appear under the system bars so that the
//                    // content doesn't resize when the system bars hide and show.
//                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Hide the nav bar and status bar
//                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
//    }
}
