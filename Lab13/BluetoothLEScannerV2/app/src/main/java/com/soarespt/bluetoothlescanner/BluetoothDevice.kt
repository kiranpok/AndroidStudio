package com.soarespt.bluetoothlescanner

import android.bluetooth.BluetoothGatt

data class BluetoothDevice(
    var name: String,
    var macAddress: String,
    var rssi: Int,
    var isConnectable: Boolean
)