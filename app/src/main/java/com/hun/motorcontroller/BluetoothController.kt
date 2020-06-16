package com.hun.motorcontroller

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Intent
import android.widget.Toast
import com.hun.motorcontroller.recycler_adapter.BTDialogRecyclerAdapter

class BluetoothController(private val activity: Activity) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    fun activateBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(activity, "블루투스 기능을 지원하지 않는 디바이스입니다.", Toast.LENGTH_SHORT).show()
        } else {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(
                enableBluetoothIntent,
                Constants.REQUEST_ENABLE_BLUETOOTH
            )
        }
    }

    fun registerReceiver() {

    }

    fun scanBluetoothDevices(adapter: BTDialogRecyclerAdapter) {
        adapter.addItem("abd", "dda")
    }

    // Return paired devices as Set
    fun getPairedDevices(): Set<BluetoothDevice>? {
        return bluetoothAdapter?.bondedDevices
    }
}
