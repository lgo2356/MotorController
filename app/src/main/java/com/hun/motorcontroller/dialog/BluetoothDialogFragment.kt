package com.hun.motorcontroller.dialog

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hun.motorcontroller.ConnectThread
import com.hun.motorcontroller.Constants
import com.hun.motorcontroller.ObservableList
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Device
import com.hun.motorcontroller.recycler_adapter.BTDialogRecyclerAdapter
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.lang.IllegalStateException
import java.lang.reflect.Method
import java.util.*
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
    private lateinit var loadingIcon: ImageView

//    private var thread: com.hun.motorcontroller.ConnectThread? = null
//    private var thread: ConnectThread? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let { fragmentActivity ->
            this.fragmentActivity = fragmentActivity
            checkPermissions(fragmentActivity)
            activateBluetooth()
            registerBluetoothReceive()

            val builder = AlertDialog.Builder(fragmentActivity)
            val inflater = requireActivity().layoutInflater
            val view: View = inflater.inflate(R.layout.layout_bluetooth_dialog, null)

            val pairedDevicesRecycler = view.findViewById<RecyclerView>(R.id.recycler_paired_devices)
            val discoveredDevicesRecycler = view.findViewById<RecyclerView>(R.id.recycler_discovered_devices)
            val pairedDeviceCover = view.findViewById<FrameLayout>(R.id.cover_paired)
            loadingIcon = view.findViewById(R.id.imageview_loading_icon)
            Glide.with(this).asGif().load(R.raw.loading02).into(loadingIcon)

            pairedDevicesRecycler.adapter = btDialogPairedAdapter
            pairedDevicesRecycler.layoutManager = LinearLayoutManager(context)
            discoveredDevicesRecycler.adapter = btDialogDiscoveredAdapter
            discoveredDevicesRecycler.layoutManager = LinearLayoutManager(context)

            // Get paired devices ...
            getPairedDevices()?.let {
                if (it.isNotEmpty()) pairedDeviceCover.visibility = View.GONE
                else {
                    pairedDeviceCover.visibility = View.VISIBLE
                }

                for (device in it) btDialogPairedAdapter.addItem(device.name, device.address)
            } ?: setPairedEmpty(pairedDeviceCover)

            scanBluetoothDevices()

            // Paired recycler 아이템 클릭 이벤트 처리
            btDialogPairedAdapter.setOnItemClickListener(
                object : BTDialogRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val devices = btDialogPairedAdapter.getItems()
                        val deviceAddress = devices[position].deviceAddress
                        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)

                        val socket = getConnectedSocket(device!!)
                    }
                })

            // Discovered recycler 아이템 클릭 이벤트 처리
            btDialogDiscoveredAdapter.setOnItemClickListener(
                object : BTDialogRecyclerAdapter.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val devices = btDialogDiscoveredAdapter.getItems()
                        val deviceAddress = devices[position].deviceAddress
                        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)

                        val socket = getConnectedSocket(device!!)
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
                    val newDevice: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    newDevice?.let {
                        val name: String? = it.name
                        val address: String = it.address

                        if (!isDuplicatedDevice(address) && !isPairedDevice(address) && name != null) {
                            btDialogDiscoveredAdapter.addItem(name, address)
                        }
                    }
                }

                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    Log.d("Receiver Action", "ACTION STATE CHANGED")
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("Receiver Action", "ACTION DISCOVERY STARTED")
                    loadingIcon.visibility = View.VISIBLE
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("Receiver Action", "ACTION DISCOVERY FINISHED")
                    loadingIcon.visibility = View.INVISIBLE
                }

                null -> { }
            }
        }
    }

    private fun progressOn() {

    }

    private fun isPairedDevice(address: String): Boolean {
        var isPaired = false
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

        if (pairedDevices != null && pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                if (address == device.address) {
                    isPaired = true
                    break
                }
            }
        }

        return isPaired
    }

    private fun isDuplicatedDevice(address: String): Boolean {
        var isDuplicated = false
        val discoveredDevices: List<Device>? = btDialogDiscoveredAdapter.getItems()

        if (discoveredDevices != null && discoveredDevices.isNotEmpty()) {
            for (device in discoveredDevices) {
                if (address == device.deviceAddress) {
                    isDuplicated = true
                    break
                }
            }
        }

        return isDuplicated
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

    private fun scanBluetoothDevices() {
        bluetoothAdapter?.cancelDiscovery()
        bluetoothAdapter?.startDiscovery()
    }

    private fun setConnectionIconToConnected() {
        val icon: ImageView? = activity?.findViewById(R.id.imgview_bluetooth_check)
        icon?.setImageResource(R.drawable.bluetooth_state_connected_shape)
    }

    private fun getConnectedSocket(device: BluetoothDevice): BluetoothSocket? {
        val bluetoothSocket = device.createRfcommSocketToServiceRecord(Constants.UUID_SERIAL_PORT)

        val observable = Observable.just(bluetoothSocket)
            .subscribeOn(Schedulers.io())
            .doOnComplete {
                setConnectionIconToConnected()
                this.dismiss()
            }
            .doOnError {

            }
            .subscribe { socket ->
                bluetoothAdapter?.cancelDiscovery()
                socket.connect()
            }

        return bluetoothSocket
    }

    class ConnectThread(device: BluetoothDevice, adapter: BluetoothAdapter?) : Thread() {

        private var mmSocket: BluetoothSocket? =
            device.createRfcommSocketToServiceRecord(Constants.UUID_SERIAL_PORT)
        private val adapter = adapter
        val device = device

        override fun run() {
            this.adapter?.cancelDiscovery()

            try {
                mmSocket?.connect()
            } catch (e: IOException) {
                e.printStackTrace()
            }

//            mSocket?.use { socket ->
//                socket.connect()
//            }
        }
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
        bluetoothAdapter?.cancelDiscovery()
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
