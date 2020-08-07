package com.hun.motorcontroller

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import android.widget.ToggleButton
import com.google.android.material.snackbar.Snackbar
import com.hun.motorcontroller.data.Motor
import com.hun.motorcontroller.dialog.BluetoothDialogFragment
import com.hun.motorcontroller.dialog.MotorNameDialogFragment
import com.hun.motorcontroller.dialog.MotorRenameDialogFragment
import com.hun.motorcontroller.dialog.MotorSettingDialogFragment
import com.hun.motorcontroller.recycler_adapter.MotorRecyclerAdapter
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.IOException

import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity() {

    private var isWriteButtonPressed = false
    private var isWriteToggled = false

    private val coroutineList: ArrayList<Job> = ArrayList()

    private val motorAdapter: MotorRecyclerAdapter = MotorRecyclerAdapter()
    private val motorRealm: Realm = Realm.getDefaultInstance()

    private lateinit var bluetoothService: BluetoothService
    private lateinit var bluetoothDialog: BluetoothDialogFragment

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        recycler_motor_buttons.adapter = motorAdapter

        handler = Handler {
            when (it.what) {
                Constants.MESSAGE_READ -> {
//                    val readBytes: ByteArray = it.obj as ByteArray
//                    val readString = String(readBytes, US_ASCII)
                    val readString: String = it.obj as String
                    text_read.text = readString
                }

                Constants.MESSAGE_WRITE -> {
//                    val readBytes: ByteArray = it.obj as ByteArray
                    val readByte: Int = it.obj as Int
//                    val readString = String(readByte, US_ASCII)
                    val readString = readByte.toString()
                    text_message.text = readString
                }

                Constants.MESSAGE_TOAST -> {
                    val message = it.obj.toString()
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }

                Constants.MESSAGE_CONNECTED -> {
                    if (::bluetoothService.isInitialized) {
                        Toast.makeText(applicationContext, "디바이스에 연결되었습니", Toast.LENGTH_SHORT).show()
//                        bluetoothService.IOThread().start()
                        bluetoothService.startRead()

                        setDeviceConnectingIcon(true)
                        bluetoothDialog.dismiss()
                    } else {
                        Toast.makeText(applicationContext, "Late init exception", Toast.LENGTH_SHORT).show()
                    }
                }

                Constants.MESSAGE_DISCONNECTED -> {
                    if (::bluetoothService.isInitialized) {
                        Toast.makeText(applicationContext, "디바이스와의 연결이 끊어졌습니다", Toast.LENGTH_SHORT).show()
                        bluetoothService.close()

                        setDeviceConnectingIcon(false)
                    } else {
                        Toast.makeText(applicationContext, "Late init exception", Toast.LENGTH_SHORT).show()
                    }
                }

                Constants.MESSAGE_DEVICE -> {
                    val device: BluetoothDevice = it.obj as BluetoothDevice
                    bluetoothService.connect(device)
                }

                Constants.MESSAGE_ERROR -> {
                    val message = it.obj.toString()
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

//                    setDeviceConnectingIcon(false)
                    bluetoothDialog.dismiss()
                }
            }
            true
        }

        bluetoothService = BluetoothService(handler)

        motorAdapter.motors = motorRealm.where(Motor::class.java).findAll()
        motorRealm.addChangeListener { motorAdapter.notifyDataSetChanged() }

        if (motorAdapter.motors.isEmpty()) {
            val motorSettingDialog = MotorSettingDialogFragment()
            motorSettingDialog.show(supportFragmentManager, "missiles")
        }

        motorAdapter.setOnButtonTouchListener(object : MotorRecyclerAdapter.OnButtonTouchListener {
            override fun onButtonTouchActionDown(view: View, motionEvent: MotionEvent, position: Int) {
                isWriteButtonPressed = true

                if (coroutineList.size > 0) {
                    for (job in coroutineList) {
                        job.cancel()
                    }
                    coroutineList.clear()
                }

                if (::bluetoothService.isInitialized) {
                    if (bluetoothService.isConnected()) {
                        sendPacketManually(position)
                    } else {
                        showSnackBar("블루투스를 연결해주세요")
                    }
                } else {
                    Toast.makeText(applicationContext, "Late init exception", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onButtonTouchActionUp(view: View, motionEvent: MotionEvent, position: Int) {
                isWriteButtonPressed = false
            }

            override fun onButtonTouchActionCancel(view: View, motionEvent: MotionEvent, position: Int) {
                isWriteButtonPressed = false
            }
        })

        motorAdapter.setOnToggleClickListener(object : MotorRecyclerAdapter.OnToggleClickListener {
            override fun onToggleClick(view: View, position: Int, isChecked: Boolean) {
                isWriteToggled = (view as ToggleButton).isChecked

                if (coroutineList.size > 0) {
                    for (job in coroutineList) {
                        job.cancel()
                    }
                    coroutineList.clear()
                }

                if (::bluetoothService.isInitialized) {
                    if (bluetoothService.isConnected()) {
                        sendPacketAutomatically(position)
                    } else {
                        showSnackBar("블루투스를 연결해주세요")
                    }
                } else {
                    Toast.makeText(applicationContext, "Late init exception", Toast.LENGTH_SHORT).show()
                }
            }
        })

        motorAdapter.setOnIconClickListener(object : MotorRecyclerAdapter.OnIconClickListener {
            override fun onIconClick(view: View, position: Int) {
                MotorRenameDialogFragment().apply {
                    val args = Bundle()
                    args.putInt("position", position)
                    this.arguments = args
                    this.show(this@MainActivity.supportFragmentManager, "missiles")
                }
            }
        })
    }

    private fun setDeviceConnectingIcon(isConnecting: Boolean) {
        if (isConnecting) {
            image_lamp.setImageResource(R.drawable.bluetooth_state_connected_shape)
        } else {
            image_lamp.setImageResource(R.drawable.bluetooth_state_disconnected_shape)
        }
    }

//    private fun clearMotorList() {
//        Realm.getDefaultInstance().use {
//            val results = it.where(Motor::class.java).findAll()
//
//            it.executeTransaction {
//                results.deleteAllFromRealm()
//            }
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_bluetooth_setting -> {
                bluetoothDialog = BluetoothDialogFragment(handler)

                if (::bluetoothDialog.isInitialized) {
                    bluetoothDialog.show(supportFragmentManager, "missiles")
                }

                true
            }

            R.id.action_motor_preference -> {
//                clearMotorList()
                val motorSettingDialog = MotorSettingDialogFragment()
                motorSettingDialog.show(supportFragmentManager, "missiles")
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.REQUEST_ENABLE_BLUETOOTH -> {
                if (resultCode == Activity.RESULT_OK) {
//                    Toast.makeText(this, "Bluetooth 설정에 성공했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Bluetooth 설정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendPacketManually(position: Int) {
        val writeManuallyScope = CoroutineScope(Dispatchers.IO).launch {
            try {
                var count = 0

                while (bluetoothService.isConnected()) {
                    if (isWriteButtonPressed) {
                        Log.d("Debug", "Manual mode 1 - ${count++}")
                        val bytes: ByteArray = byteArrayOf(0x68, position.toByte(), 0x01, 0x7E)
                        bluetoothService.writeBytes(bytes)
                    } else {
                        Log.d("Debug", "Manual mode 2 - ${count++}")
                        val bytes: ByteArray = byteArrayOf(0x68, position.toByte(), 0x00, 0x7E)
                        bluetoothService.writeBytes(bytes)
                    }
                    delay(5)
                }
            } catch (e: IOException) {
                Log.d("Debug", "Error from writeManuallyScope", e)
                this.cancel()
            }
        }
        coroutineList.add(writeManuallyScope)
    }

    private fun sendPacketAutomatically(position: Int) {
        val writeAutomaticallyScope = CoroutineScope(Dispatchers.IO).launch {
            try {
                var count = 0

                while (bluetoothService.isConnected()) {
                    if (isWriteToggled) {
                        Log.d("Debug", "Auto mode 1 - ${count++}")
                        val bytes: ByteArray = byteArrayOf(0x68, position.toByte(), 0x01, 0x7E)
                        bluetoothService.writeBytes(bytes)
                    } else {
                        Log.d("Debug", "Auto mode 2 - ${count++}")
                        val bytes: ByteArray = byteArrayOf(0x68, position.toByte(), 0x00, 0x7E)
                        bluetoothService.writeBytes(bytes)
                    }
                    delay(5)
                }
            } catch (e: IOException) {
                Log.d("Debug", "Error from writeAutomaticallyScope", e)
                this.cancel()
            }
        }
        coroutineList.add(writeAutomaticallyScope)
    }

    private fun showSnackBar(message: String) {
        val snackBar = Snackbar.make(container_main, message, Snackbar.LENGTH_INDEFINITE)
        snackBar.setAction("확인") { snackBar.dismiss() }.show()
    }

    private fun registerBluetoothReceiver() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        this.registerReceiver(receiver, filter)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.d("Debug", "Bluetooth device disconnected")
                    setDeviceConnectingIcon(false)
                    bluetoothService.close()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Debug", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Debug", "onResume")
        registerBluetoothReceiver()
    }

    override fun onPause() {
        super.onPause()
        Log.d("Debug", "onPause")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("Debug", "onRestart")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Debug", "onStop")

        try {
            this.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            Log.d("Debug", "Failed to unregister receiver", e)
//            val errorMsg = handler.obtainMessage(Constants.MESSAGE_ERROR, "Failed to unregister receiver")
//            errorMsg.sendToTarget()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Debug", "onDestroy")

        if (!motorRealm.isClosed) {
            try {
                motorRealm.close()
            } catch (e: IllegalArgumentException) {
                Log.d("Debug", "Failed to close the Realm object", e)
            }
        }

        if (::bluetoothService.isInitialized) {
            bluetoothService.close()
        } else {
            Log.d("Debug", "BluetoothService is not initialized")
        }
    }
}
