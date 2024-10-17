package com.example.heartrategraph

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class MyViewModel: ViewModel() {

    private val _rssiList = MutableLiveData<List<Int>>(emptyList())
    val rssiList: LiveData<List<Int>> get() = _rssiList
    private val _missingPermission = MutableLiveData<Boolean>()
    val missingPermission: LiveData<Boolean> get() = _missingPermission
    private var isScanning = false
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    fun startScanning(bluetoothAdapter: BluetoothAdapter, context: Context) {
        if (!isScanning) {
            if (hasBluetoothScanPermission(context)) {
                try{
                    isScanning = true
                    bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
                    bluetoothLeScanner?.startScan(scanCallback) // Start BLE scan
                } catch(e: SecurityException) {
                    _missingPermission.postValue(true) // Signal that permission is missing
                }
            } else {
                _missingPermission.postValue(true)
            }
        }
    }
    fun stopScanning(context: Context){
        if (isScanning){
            if (hasBluetoothScanPermission(context)){
                try{
                    isScanning= false
                    bluetoothLeScanner?.stopScan(scanCallback)
                } catch (e:SecurityException){
                    _missingPermission.postValue(true)
                }            }
        }
    }
    private fun hasBluetoothScanPermission(context: Context): Boolean{
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)== PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    private val scanCallback = object : ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let {
                val rssi = it.rssi
                updateRssiList(rssi)
            }
        }
        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                val rssi = result.rssi
                updateRssiList(rssi)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            // Handle scan failure
        }
    }

    private fun updateRssiList(newRssi: Int) {
        val updatedList = _rssiList.value?.toMutableList() ?: mutableListOf()
        updatedList.add(newRssi)
        _rssiList.postValue(updatedList)
    }
}



