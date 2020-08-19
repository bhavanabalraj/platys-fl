package com.example.platys.worker.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.platys.worker.SensorDataUpdateCallback

class BluetoothDiscoveryHelper {
    private var bluetoothAdapter: BluetoothAdapter
    private var bluetoothReceiver: BroadcastReceiver
    private var bluetoothUpdateCallback: SensorDataUpdateCallback<BleDataPoint>? = null
    private var pairedDevices: MutableList<BleDataPoint>
    private var pairedDevicesIDs: MutableSet<String>

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        pairedDevices = mutableListOf()
        pairedDevicesIDs = mutableSetOf()

        bluetoothReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        // Discovery has found a device. Get the BluetoothDevice
                        // object and its info from the Intent.
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                        if(device != null) {
                            if(!pairedDevicesIDs.contains(device.address)) {
                                bluetoothUpdateCallback?.onUpdate(
                                    BleDataPoint(
                                        device.name,
                                        device.address,
                                        device.type,
                                        device.bluetoothClass?.deviceClass,
                                        device.bondState,
                                        pairedDevices
                                    )
                                )
                                return
                            }
                        }

                        bluetoothUpdateCallback?.onUpdate(
                            BleDataPoint(
                                "", "", -1, -1, -1, pairedDevices
                            )
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    fun startDiscovery(context: Context) {
        val bondedDevices = bluetoothAdapter.bondedDevices

        for(device in bondedDevices) {
            if(!pairedDevicesIDs.contains(device.address)) {
                pairedDevices.add(
                    BleDataPoint(
                        device.name,
                        device.address,
                        device.type,
                        device.bluetoothClass.deviceClass,
                        device.bondState
                    )
                )
                pairedDevicesIDs.add(device.address)
            }
        }

        if(bluetoothAdapter.isEnabled) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            context.registerReceiver(bluetoothReceiver, filter)
            bluetoothAdapter.startDiscovery()
        } else {
            bluetoothUpdateCallback?.onUpdate(
                BleDataPoint(
                    "", "", -1, -1, -1, pairedDevices
                )
            )
        }
    }

    fun stopDiscovery(context: Context) {
        context.unregisterReceiver(bluetoothReceiver)
    }

    fun setOnChangeListener(callback: SensorDataUpdateCallback<BleDataPoint>) {
        bluetoothUpdateCallback = callback
    }
}