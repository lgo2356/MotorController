package com.hun.motorcontroller.dialog

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hun.motorcontroller.Constants
import com.hun.motorcontroller.ObservableList
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Device
import com.hun.motorcontroller.recycler_adapter.BTDialogRecyclerAdapter
import java.lang.IllegalStateException
import kotlin.collections.ArrayList

class BluetoothDialogFragment : DialogFragment() {

    private val btPairedDevices: ArrayList<Device> = ArrayList()
    private val btDiscoveredDevices: ArrayList<Device> = ArrayList()
    private val observableList: ObservableList<Device> = ObservableList()
    private val btDialogPairedAdapter = BTDialogRecyclerAdapter(btPairedDevices)
    private val btDialogDiscoveredAdapter = BTDialogRecyclerAdapter(btDiscoveredDevices)
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    private var fragmentActivity: FragmentActivity? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let { fragmentActivity ->
            this.fragmentActivity = fragmentActivity
            checkPermissions(fragmentActivity)
            activateBluetooth()
            registerBluetoothReceive()

            val builder = AlertDialog.Builder(fragmentActivity)
            val inflater = requireActivity().layoutInflater
            val view: View = inflater.inflate(R.layout.layout_bluetooth_dialog, null)
            val pairedDevicesRecycler =
                view.findViewById<RecyclerView>(R.id.recycler_paired_devices)
            val discoveredDevicesRecycler =
                view.findViewById<RecyclerView>(R.id.recycler_discovered_devices)

            pairedDevicesRecycler.adapter = btDialogPairedAdapter
            pairedDevicesRecycler.layoutManager = LinearLayoutManager(context)
            discoveredDevicesRecycler.adapter = btDialogDiscoveredAdapter
            discoveredDevicesRecycler.layoutManager = LinearLayoutManager(context)

            // Get paired devices ...
            val pairedDeviceCover = view.findViewById<FrameLayout>(R.id.cover_paired)
            getPairedDevices()?.let {
                if (it.isNotEmpty()) pairedDeviceCover.visibility = View.GONE
                else {
                    pairedDeviceCover.visibility = View.VISIBLE
                }

                for (device in it) btDialogPairedAdapter.addItem(device.name, device.address)
            } ?: setPairedEmpty(pairedDeviceCover)

            // Start bluetooth devices scan -> discovered -> list up

//            bluetoothController!!.scanBluetoothDevices()

            // 생성자 = scan, 소비자 = adapter

//            btPairedDevices.getObservable()
//                .subscribe { device ->
//                    Log.d("Test", device.deviceName)
//                }
//            btPairedDevices.add("DD 01")
            scanBluetoothDevices()

            // Paired recycler 항목 이벤트 리스너 객체 생성
            btDialogPairedAdapter.setOnItemClickListener(
                object : BTDialogRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
                        btDialogPairedAdapter.addItem("Device 02", "Paired")
                    }
                })

            // Discovered recycler 항목 이벤트 리스터 객체 생성
            btDialogDiscoveredAdapter.setOnItemClickListener(
                object : BTDialogRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        Toast.makeText(context, "Position: $position", Toast.LENGTH_SHORT).show()
                        btDialogDiscoveredAdapter.addItem("Device 02", "Discovered")
                    }
                })

            builder
                .setView(view)
                .setMessage("블루투스 설정")
                .setNegativeButton("취소") { _, _ -> }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun checkPermissions(activity: Activity) {
        val requiredPermissions: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val accessFineLocationPermission =
            ContextCompat.checkSelfPermission(activity, requiredPermissions[0])

        if (accessFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            return
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    requiredPermissions[0]
                )
            ) {
                // Explain to get permissions
                // '확인' -> get permissions
                val permissionDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
                permissionDialog
                    .setTitle("권한 요청")
                    .setMessage("블루투스 연결을 위해 디바이스의 위치 정보 접근 권한이 필요합니다.")
                    .setPositiveButton("확인") { _, _ ->
                        ActivityCompat.requestPermissions(
                            activity,
                            requiredPermissions,
                            Constants.REQUEST_PERMISSIONS
                        )
                    }
                    .setNegativeButton("취소") { _, _ -> }
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    requiredPermissions,
                    Constants.REQUEST_PERMISSIONS
                )
            }
        }
    }

    // Bluetooth receiver actions 핸들링
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val newDevice: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    newDevice?.let {
                        val name: String? = it.name
                        val address: String = it.address

                        if (isDuplicationAddress(address)) {
                            btDialogDiscoveredAdapter.removeItem(address)
                            btDialogDiscoveredAdapter.addItem(name ?: "Empty", address)
                        } else {
                            btDialogDiscoveredAdapter.addItem(name ?: "Empty", address)
                        }
                    }
                }

                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    Log.d("Receiver Action", "ACTION STATE CHANGED")
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("Receiver Action", "ACTION DISCOVERY STARTED")
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("Receiver Action", "ACTION DISCOVERY FINISHED")
                }

                null -> {
                }
            }
        }
    }

    private fun isDuplicationAddress(address: String): Boolean {
        var isDuplication = false
        val discoveredDevices: List<Device>? = btDialogDiscoveredAdapter.getItems()

        if (discoveredDevices != null && discoveredDevices.isNotEmpty()) {
            for (device in discoveredDevices) {
                if (address == device.deviceAddress) {
                    isDuplication = true
                    break
                }
            }
        }
        return isDuplication
    }

    private fun registerBluetoothReceive() {
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_FOUND)
        }
        fragmentActivity?.registerReceiver(receiver, filter)
    }

    private fun activateBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(fragmentActivity, "블루투스 기능을 지원하지 않는 디바이스입니다.", Toast.LENGTH_SHORT).show()
        } else {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            fragmentActivity?.startActivityForResult(
                enableBluetoothIntent,
                Constants.REQUEST_ENABLE_BLUETOOTH
            )
        }
    }

    fun scanBluetoothDevices() {
        bluetoothAdapter?.startDiscovery()
    }

    // Return paired devices as Set
    fun getPairedDevices(): Set<BluetoothDevice>? {
        return bluetoothAdapter?.bondedDevices
    }

    private fun setPairedEmpty(cover: FrameLayout) {
        cover.visibility = View.VISIBLE
//        cover.layoutParams = ConstraintLayout.LayoutParams(
//            ConstraintLayout.LayoutParams.MATCH_PARENT,
//            ConstraintLayout.LayoutParams.WRAP_CONTENT
//        )
    }

    override fun onStop() {
        super.onStop()
        fragmentActivity?.unregisterReceiver(receiver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
