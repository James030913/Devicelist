package com.example.myapplication


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var mBtAdapter: BluetoothAdapter? = null
    private var mPairedDevicesArrayAdapter: ArrayAdapter<String>? = null
    private var mNewDevicesArrayAdapter: ArrayAdapter<String>? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanButton: Button = findViewById<View>(R.id.button_scan) as Button
        scanButton.setOnClickListener {
            doDiscovery()
        }

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)
        mNewDevicesArrayAdapter = ArrayAdapter(this, R.layout.device_name)

        // Find and set up the ListView for paired devices

        // Find and set up the ListView for paired devices
        val pairedListView: ListView = findViewById<View>(R.id.paired_devices) as ListView
        pairedListView.setAdapter(mPairedDevicesArrayAdapter)
//        pairedListView.setOnItemClickListener(mDeviceClickListener)

        // Find and set up the ListView for newly discovered devices
        val newDevicesListView: ListView = findViewById<View>(R.id.new_devices) as ListView
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter)
//        newDevicesListView.setOnItemClickListener(mDeviceClickListener)

        // Register for broadcasts when a device is discovered

        // Register for broadcasts when a device is discovered
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished

        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        // Get a set of currently paired devices
        val pairedDevice: Set<BluetoothDevice>? = mBtAdapter?.bondedDevices
        if (pairedDevice != null) {

            pairedDevice.forEachIndexed { index, device ->
                mPairedDevicesArrayAdapter!!.add(device.name + device.address)

            }


        }
    }
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from the Intent
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // If it's already paired, skip it, because it's been listed already
                if (device!!.bondState != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter!!.add(
                        """
                        ${device.name}
                        ${device.address}
                        """.trimIndent()
                    )
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setProgressBarIndeterminateVisibility(false)
                setTitle("selected")
                if (mNewDevicesArrayAdapter!!.count == 0) {
                    val noDevices = "No devices found"
                    mNewDevicesArrayAdapter!!.add(noDevices)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun doDiscovery() {
        if (mBtAdapter?.isDiscovering == true) {
            mBtAdapter?.cancelDiscovery()
        }

        // Request discover from BluetoothAdapter
        mBtAdapter?.startDiscovery()
    }
}