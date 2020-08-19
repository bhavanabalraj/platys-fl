package com.example.platys.tagcontext

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.rotationMatrix
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.platys.R
import com.example.platys.data.EventObserver
import com.example.platys.databinding.TagContextFragmentBinding
import com.example.platys.databinding.TagContextWindowBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class TagContextWindowFragment: DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<ContextTagViewModel> { viewModelFactory }

    private lateinit var textInputLayout: TextInputLayout

    private lateinit var chipGroup: ChipGroup

    private lateinit var tvSuggestionLabel: TextView

    private lateinit var suggestionChipGroup: ChipGroup

    private lateinit var viewDataBinding: TagContextWindowBinding

    private val args: TagContextWindowFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.tag_context_window, container, false)
        viewDataBinding = TagContextWindowBinding.bind(root).apply {
            this.viewmodel = viewModel
        }

        textInputLayout = root.findViewById(R.id.tlTag)

        chipGroup = root.findViewById(R.id.chipGroup)
        suggestionChipGroup = root.findViewById(R.id.cgSuggestion)
        tvSuggestionLabel = root.findViewById(R.id.tvSuggestionLabel)

        textInputLayout.setEndIconOnClickListener {
            viewModel.optionAdded(textInputLayout.editText!!.text.toString(), true, -1)
        }

        val tagButton = root.findViewById<MaterialButton>(R.id.tagButton)

        tagButton.setOnClickListener {
            viewModel.tagWindowClosed()
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setUpObservers()
        viewModel.viewLoaded(args.cardId)
        viewModel.loadSuggestions()
    }

    private fun setUpObservers() {
        viewModel.addChip.observe(viewLifecycleOwner, EventObserver { data ->
            chipGroup.addView(createChip(data, true))
        })

        viewModel.clearTextField.observe(viewLifecycleOwner, EventObserver {
            textInputLayout.editText!!.text.clear()
        })

        viewModel.suggestions.observe(viewLifecycleOwner, EventObserver { data ->
            suggestionChipGroup.visibility = View.VISIBLE
            tvSuggestionLabel.visibility = View.VISIBLE

            for(suggestion in data) {
                suggestionChipGroup.addView(createChip(suggestion, false))
            }
        })

        viewModel.backPressed.observe(viewLifecycleOwner, EventObserver {
            requireActivity().onBackPressed()
        })

        viewModel.hint.observe(viewLifecycleOwner, EventObserver { myHint ->
            textInputLayout.hint = myHint
        })

        viewModel.checkChip.observe(viewLifecycleOwner, EventObserver { id ->
            val chip = suggestionChipGroup.findViewById<Chip>(id)
            if(chip != null) {
                chip.isChecked = true
            }
        })
    }

    private fun createChip(chipModel: ChipModel, remove: Boolean) : Chip {
        val chip = Chip(requireContext())
        if(remove) {
            chip.setChipDrawable(ChipDrawable.createFromResource(requireContext(), R.xml.standlone_chip))
        } else {
            chip.setChipDrawable(ChipDrawable.createFromResource(requireContext(), R.xml.standalone_suggestion_chip))
        }

        chip.id = chipModel.chipID
        chip.text = chipModel.chipText
        chip.setTextColor(resources.getColor(android.R.color.black, null))

        if(remove) {
            chip.setOnCloseIconClickListener {
                viewModel.chipCloseClicked(chip.text.toString())
                chipGroup.removeView(chip)
                val dupChip = suggestionChipGroup.findViewById<Chip>(chip.id)
                if(dupChip != null) {
                    dupChip.isChecked = false
                }
            }
        } else {
            chip.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    viewModel.optionAdded(chip.text.toString(), false, chip.id)
                } else {
                    viewModel.chipCloseClicked(chip.text.toString())
                    val dupChip = chipGroup.findViewById<Chip>(chip.id)
                    if(dupChip != null) {
                        chipGroup.removeView(dupChip)
                    }
                }
            }
        }

        return chip
    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        location = Location(activity as AppCompatActivity, object: LocationListener {
//            override fun locationResponse(locationResult: LocationResult) {
//                lat = locationResult.lastLocation.latitude.toString()
//                long = locationResult.lastLocation.longitude.toString()
//                //Toast.makeText(this@MainActivity, "Lat:$lat Long:$long", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//
//        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
//
//        when {
//            bluetoothAdapter == null -> {
//                Toast.makeText(this, "Device does not support bluetooth", Toast.LENGTH_SHORT).show()
//            }
//            bluetoothAdapter.isEnabled -> {
//                //Toast.makeText(activity, "Bluetooth is switched on", Toast.LENGTH_SHORT).show()
//            }
//            else -> {
//                //Toast.makeText(activity, "Bluetooth is switched off", Toast.LENGTH_SHORT).show()
//                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                startActivityForResult(enableBtIntent, 10)
//            }
//        }
//
//        val sensorManager = (activity as AppCompatActivity).getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val gravitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
//        val linearAccSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
//        val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
//        val stepCountSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//        val accSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        val gySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
//
//        gravitySensor?.also { gravity ->
//            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//
//        linearAccSensor?.also { gravity ->
//            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//
//        rotationSensor?.also { gravity ->
//            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//
//        stepCountSensor?.also { gravity ->
//            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//
//        accSensor?.also { gravity ->
//            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//
//        gySensor?.also { gravity ->
//            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//
//        val wifiScanReceiver = object : BroadcastReceiver() {
//
//            override fun onReceive(context: Context, intent: Intent) {
//                val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
//                } else {
//                    TODO("VERSION.SDK_INT < M")
//                }
//                if (success) {
//                    scanSuccess()
//                } else {
//                    scanFailure()
//                }
//            }
//        }
//
//        wifiManager = (activity as AppCompatActivity).applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
//        (activity as AppCompatActivity).registerReceiver(wifiScanReceiver, intentFilter)
//
//        val success = wifiManager!!.startScan()
//        if (!success) {
//            // scan failure handling
//            scanFailure()
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        location?.initializeLocation()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        location?.stopUpdateLocation()
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when(requestCode) {
//            10 -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    turnBluetooth()
//                } else {
//                    Toast.makeText(activity, "You did not give permissions to access bluetooth", Toast.LENGTH_SHORT).show()
//                }
//            }
//            else -> { }
//        }
//    }
//
//    private val btStateChangeListener = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val newState = intent?.extras?.get("EXTRA_STATE")
//            val oldState = intent?.extras?.get("EXTRA_PREVIOUS_STATE")
//
//            if(newState == BluetoothAdapter.STATE_ON && (oldState == BluetoothAdapter.STATE_OFF || oldState == BluetoothAdapter.STATE_TURNING_OFF)) {
//                turnBluetooth()
//            } else if(newState == BluetoothAdapter.STATE_OFF && (oldState == BluetoothAdapter.STATE_ON || oldState == BluetoothAdapter.STATE_TURNING_ON)) {
//                Toast.makeText(activity, "You turned off bluetooth. This application needs bluetooth to be switched on", Toast.LENGTH_SHORT).show()
//                unregisterReceivers()
//            }
//        }
//    }
//
//    // Create a BroadcastReceiver for ACTION_FOUND.
//    private val btReceiver = object : BroadcastReceiver() {
//
//        override fun onReceive(context: Context, intent: Intent) {
//            val action: String? = intent.action
//            when(action) {
//                BluetoothDevice.ACTION_FOUND -> {
//                    // Discovery has found a device. Get the BluetoothDevice
//                    // object and its info from the Intent.
//                    val device: BluetoothDevice? =
//                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//                    val deviceName = device!!.name
//                    val deviceHardwareAddress = device!!.address // MAC address
//                }
//            }
//        }
//    }
//
//    private fun scanSuccess() {
//        val results = wifiManager!!.scanResults
//    }
//
//    private fun scanFailure() {
//        // handle failure: new scan did NOT succeed
//        // consider using old scan results: these are the OLD results!
//        //val results = wifiManager.scanResults
//        if(wifiManager!!.isWifiEnabled) {
//            Toast.makeText(activity, "Wifi scan failed", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(activity, "Please turn on wifi", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    fun turnBluetooth() {
//        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        registerReceiver(btReceiver, filter)
//
//        val filter1 = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
//        registerReceiver(btStateChangeListener, filter1)
//    }
//
//    fun unregisterReceivers() {
//        unregisterReceiver(btReceiver)
//        unregisterReceiver(btStateChangeListener)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        unregisterReceivers()
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//    }
//
//    override fun onSensorChanged(event: SensorEvent) {
//        when (event.sensor.type) {
//            Sensor.TYPE_GRAVITY -> {
//                val x = event.values[0]
//                val y = event.values[1]
//                val z = event.values[2]
//            }
//            Sensor.TYPE_LINEAR_ACCELERATION -> {
//                val x = event.values[0]
//                val y = event.values[1]
//                val z = event.values[2]
//            }
//            Sensor.TYPE_ROTATION_VECTOR -> {
//                val x = event.values[0]
//                val y = event.values[1]
//                val z = event.values[2]
//                val scalar = event.values[3]
//            }
//            Sensor.TYPE_STEP_COUNTER -> {
//                val steps = event.values[0]
//            }
//            Sensor.TYPE_ACCELEROMETER -> {
//                val x = event.values[0]
//                val y = event.values[1]
//                val z = event.values[2]
//            }
//            Sensor.TYPE_GYROSCOPE -> {
//                val x = event.values[0]
//                val y = event.values[1]
//                val z = event.values[2]
//            }
//        }
//    }
}