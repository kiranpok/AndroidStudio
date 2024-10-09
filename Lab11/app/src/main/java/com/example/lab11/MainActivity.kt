package com.example.lab11

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private var mBluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the BluetoothAdapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        // Request permissions if not already granted
        if (!hasPermissions()) {
            return
        }

        // Set up the UI using Jetpack Compose
        setContent {
            // Show the BLE devices and scan controls
            ShowDevices(mBluetoothAdapter!!)
        }
    }

    // Check for required permissions (Bluetooth and Location)
    private fun hasPermissions(): Boolean {
        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            Log.d("DBG", "No Bluetooth LE capability")
            return false
        } else if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DBG", "Requesting Location Permission")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return false
        }
        return true
    }
}

class MyViewModel : ViewModel() {
    val scanResults = MutableLiveData<List<ScanResult>>(null)
    val fScanning = MutableLiveData<Boolean>(false)

    private val mResults = HashMap<String, ScanResult>()

    // Function to scan devices using the BluetoothLeScanner
    fun scanDevices(scanner: BluetoothLeScanner) {
        viewModelScope.launch(Dispatchers.IO) {
            fScanning.postValue(true)

            val settings = android.bluetooth.le.ScanSettings.Builder()
                .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build()

            // Start BLE scan
            scanner.startScan(null, settings, leScanCallback)

            // Stop scanning after a set period (5 seconds)
            delay(SCAN_PERIOD)
            scanner.stopScan(leScanCallback)

            // Post the results to LiveData to update the UI
            scanResults.postValue(mResults.values.toList())
            fScanning.postValue(false)
        }
    }

    // ScanCallback to handle results of BLE scan
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val deviceAddress = result.device.address
            mResults[deviceAddress] = result  // Store the result to avoid duplicates
        }
    }

    companion object {
        const val SCAN_PERIOD: Long = 5000  // Scan period in milliseconds
    }
}

@Composable
fun ShowDevices(
    mBluetoothAdapter: BluetoothAdapter,
    model: MyViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // Get context and observe scanning results and scanning state
    val context = LocalContext.current
    val scanResults: List<ScanResult>? by model.scanResults.observeAsState(null)
    val fScanning: Boolean by model.fScanning.observeAsState(false)

    // Layout to display the UI
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Scan button
        Button(
            onClick = {
                val scanner = mBluetoothAdapter.bluetoothLeScanner
                scanner?.let { model.scanDevices(it) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (fScanning) "Scanning..." else "Start Scanning")
        }

        // Spacer between button and results
        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of found BLE devices
        if (scanResults.isNullOrEmpty()) {
            Text(text = "No devices found", modifier = Modifier.padding(16.dp), color = Color.Gray)
        } else {
            scanResults?.forEach { result ->
                BluetoothDeviceItemView(result.device.name ?: "Unnamed Device", result.device.address, result.rssi)
            }
        }
    }
}

@Composable
fun BluetoothDeviceItemView(name: String, address: String, rssi: Int) {
    // Layout for individual device information
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Text(text = "Name: $name")
        Text(text = "Address: $address")
        Text(text = "RSSI: $rssi dBm")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    // Dummy preview to see layout in Compose preview (won't scan devices)
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Bluetooth Device Scanner")
        BluetoothDeviceItemView(name = "Device 1", address = "00:11:22:33:44:55", rssi = -45)
        BluetoothDeviceItemView(name = "Device 2", address = "00:11:22:33:44:66", rssi = -70)
    }
}
