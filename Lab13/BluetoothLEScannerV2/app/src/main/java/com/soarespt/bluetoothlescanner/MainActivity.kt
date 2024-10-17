package com.soarespt.bluetoothlescanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.soarespt.bluetoothlescanner.ui.theme.BluetoothLEScannerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val permissionsHelper = PermissionsHelper()

    var bpm by mutableStateOf(0)
    var isConnected by mutableStateOf(false) // Move to be a shared state

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
                setIsConnected(true) // Update connection state
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("DBG", "Disconnected from GATT server.")
                setIsConnected(false)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val heartRateService = gatt.getService(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"))
                val characteristic = heartRateService?.getCharacteristic(UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb"))

                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            } else {
                Log.w("DBG", "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val bpmValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1)
            bpm = bpmValue
            Log.d("DBG", "BPM: $bpm")
        }
    }

    fun setIsConnected(connected: Boolean) {
        isConnected = connected
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        enableEdgeToEdge()

        setContent {
            BluetoothLEScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var hasPermissions by remember { mutableStateOf(false) }
                    var bluetoothEnabled by remember { mutableStateOf(bluetoothAdapter.isEnabled) }
                    val context = LocalContext.current
                    val requestPermissionsLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions -> hasPermissions = permissions.values.all { it } }

                    LaunchedEffect(Unit) {
                        val missingPermissions = permissionsHelper.checkPermissions(context)
                        if (missingPermissions.isNotEmpty()) {
                            requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
                        } else {
                            hasPermissions = true
                        }
                    }

                    val bluetoothEnablerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == RESULT_OK) {
                            bluetoothEnabled = bluetoothAdapter.isEnabled
                        }
                    }

                    if (!bluetoothEnabled) {
                        PromptEnableBluetooth(onEnableBluetooth = {
                            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            bluetoothEnablerLauncher.launch(enableBtIntent)
                        })
                    } else if (hasPermissions) {
                        MainContent(
                            bluetoothLeScanner,
                            requestPermissionsLauncher,
                            bpm,
                            bluetoothAdapter,
                            gattCallback,
                            isConnected // Pass isConnected state
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                PermissionMessage("Bluetooth and Location Permissions are required to use this app.")
                            } else {
                                PermissionMessage("Location Permissions are required to use this app.")
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun PromptEnableBluetooth(onEnableBluetooth: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Bluetooth is turned off. Please enable Bluetooth to use this app.",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .padding(30.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface), shape = MaterialTheme.shapes.medium)
                    .padding(30.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onEnableBluetooth) {
                Text(text = "Enable Bluetooth")
            }
        }
    }
}

@Composable
fun PermissionMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .padding(30.dp)
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface), shape = MaterialTheme.shapes.medium)
                .padding(30.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun MainContent(
    bluetoothLeScanner: BluetoothLeScanner,
    requestPermissionsLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    bpm: Int,
    bluetoothAdapter: BluetoothAdapter,
    gattCallback: BluetoothGattCallback,
    isConnected: Boolean
) {
    val coroutineScope = rememberCoroutineScope()  // Coroutine scope for background tasks
    val devices = remember { mutableStateListOf<BluetoothDevice>() }
    var isScanning by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val permissionsHelper = PermissionsHelper()

    var connectedDeviceName by remember { mutableStateOf<String?>(null) }
    var connectedDeviceMac by remember { mutableStateOf<String?>(null) }

    // Define the scan callback
    val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: "Unknown"
            val macAddress = device.address
            val rssi = result.rssi
            val isConnectable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) result.isConnectable else false

            // Add to the device list if not already present
            val existingDevice = devices.find { it.macAddress == macAddress }
            if (existingDevice == null) {
                devices.add(BluetoothDevice(name, macAddress, rssi, isConnectable))
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanFailed", "Scan failed with error: $errorCode")
        }
    }

    // UI layout
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Start scanning button
            Button(
                onClick = {
                    val missingPermissions = permissionsHelper.checkPermissions(context)
                    if (missingPermissions.isEmpty()) {
                        isScanning = true
                        devices.clear()
                        coroutineScope.launch {
                            try {
                                bluetoothLeScanner.startScan(scanCallback)  // Start scan in background
                                delay(3000)  // Scan for 3 seconds
                            } catch (e: SecurityException) {
                                Log.e("SecurityException", "Scan failed: ${e.message}")
                            } finally {
                                bluetoothLeScanner.stopScan(scanCallback)  // Stop scan after delay
                                isScanning = false
                            }
                        }
                    } else {
                        requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .height(64.dp),
                shape = RectangleShape
            ) {
                Text(
                    text = if (isScanning) "Scanning..." else "Start Scanning",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Show connected device info
            if (isConnected) {
                Text(
                    text = "Device: ${connectedDeviceName ?: "Unknown"}\nMAC: ${connectedDeviceMac ?: "Unknown"}\nBPM: $bpm",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // List of scanned devices
            LazyColumn {
                items(devices) { device ->
                    val textStyle = if (device.isConnectable) {
                        TextStyle(color = MaterialTheme.colorScheme.onBackground)
                    } else {
                        TextStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Text(
                        text = "${device.macAddress} ${device.name} ${device.rssi}dBm",
                        style = textStyle,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                val bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.macAddress)
                                bluetoothDevice.connectGatt(context, false, gattCallback)
                                // Store device info on connect
                                connectedDeviceName = device.name
                                connectedDeviceMac = device.macAddress
                            }
                    )
                }
            }
        }
    }
}
