package com.example.heartrategraph

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.heartrategraph.ui.theme.HeartRateGraphTheme
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet



class MainActivity : ComponentActivity() {

    private val viewModel: MyViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Register permission launcher for requesting Bluetooth permissions
        val bluetoothPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                    permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (granted) {
                // Permissions granted, start scanning
                viewModel.startScanning(BluetoothAdapter.getDefaultAdapter(), this)
            } else {
                // Permissions denied, handle accordingly
                Toast.makeText(this, "Permissions denied, cannot scan for Bluetooth devices", Toast.LENGTH_SHORT).show()
            }
        }
        setContent {
            HeartRateGraphTheme() {
                MainBluetoothScreen(viewModel = viewModel, onRequestPermissions = {
                    // Request Bluetooth and location permissions when needed
                    bluetoothPermissionLauncher.launch(
                        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION)
                    )
                })
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBluetoothScreen(viewModel: MyViewModel, onRequestPermissions: () -> Unit) {
    val rssiList by viewModel.rssiList.observeAsState(emptyList())
    var showGraph by remember { mutableStateOf(false) }
    var selectedRssiList by remember { mutableStateOf(emptyList<Int>()) }
    // Check if permissions are missing and ask for them
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (!hasBluetoothPermissions(context)) {
            onRequestPermissions()
        } else {
            viewModel.startScanning(BluetoothAdapter.getDefaultAdapter(), context)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Bluetooth Scanner") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (showGraph) {
                RSSIGraphScreen(selectedRssiList) // Show the graph when true
            } else {
                if (rssiList.isEmpty()) {
                    Text("No RSSI data available")
                } else {
                    RSSIList(rssiList) { selectedList ->
                        // Set the selected list and show graph
                        selectedRssiList = selectedList
                        showGraph = true
                    }
                }
            }
        }
    }
}
@Composable
fun RSSIList(rssiList: List<Int>, onItemClick: (List<Int>) -> Unit) {
    LazyColumn {
        items(rssiList.size) { index ->
            val rssi = rssiList[index]
            Text(
                text = "Measurement $index: $rssi dBm",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(rssiList) }
                    .padding(16.dp)
            )
        }
    }
}
@Composable
fun RSSIGraphScreen(rssiList: List<Int>) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                val entries = rssiList.mapIndexed { index, rssi ->
                    Entry(index.toFloat(), rssi.toFloat())
                }
                val lineDataSet = LineDataSet(entries, "RSSI Data").apply {
                    color = Color.Blue.toArgb()
                    valueTextColor = Color.Red.toArgb()
                    lineWidth = 2f
                    circleRadius = 4f
                    setCircleColor(Color.Red.toArgb())
                }
                val lineData = LineData(lineDataSet)
                this.data = lineData
                this.invalidate() // Refresh the chart
            }
        }
    )
}
fun hasBluetoothPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}




