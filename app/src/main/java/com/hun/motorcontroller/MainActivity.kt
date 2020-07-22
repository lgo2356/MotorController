package com.hun.motorcontroller

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Toast
import com.hun.motorcontroller.data.Motor
import com.hun.motorcontroller.dialog.BluetoothDialogFragment
import com.hun.motorcontroller.dialog.MotorSettingDialogFragment
import com.hun.motorcontroller.recycler_adapter.MotorRecyclerAdapter
import io.reactivex.disposables.Disposable
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

import java.lang.Exception
import java.nio.charset.StandardCharsets.US_ASCII

class MainActivity : AppCompatActivity() {

    /**
     *  Auto mode on/off
     *  on -> write button 한 번만 클릭하면 계속 실행 패킷 전송(1),
     *  또 한 번 더 클릭하면 계속 종료 패킷 전송(2)
     *
     *  off -> write button 터치하고 있을 때만 계속 실행 패킷 전송(1),
     *  터치 안 할 때는 계속 종료 패킷 전송(2)
     */

    private var isWriteButtonPressed = false

    private val motorAdapter: MotorRecyclerAdapter = MotorRecyclerAdapter()
    private val motorRealm: Realm = Realm.getDefaultInstance()
    private var readDisposable: Disposable? = null

    //    private val bluetoothService = BluetoothService()
    private lateinit var bluetoothService: BluetoothService
    private lateinit var bluetoothDialog: BluetoothDialogFragment
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        recycler_motor_buttons.adapter = motorAdapter
//        recycler_motor_buttons.layoutManager = GridLayoutManager(this, 2)

        handler = Handler {
            when (it.what) {
                Constants.MESSAGE_READ -> {
                    val readBytes: ByteArray = it.obj as ByteArray
                    val readString = String(readBytes, US_ASCII)
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
                        Toast.makeText(applicationContext, "데이터 수신 시작", Toast.LENGTH_SHORT).show()
                        bluetoothService.IOThread().start()

                        bluetoothDialog.setConnectionIconToConnected()
                        bluetoothDialog.dismiss()
                    } else {
                        Toast.makeText(applicationContext, "Late init exception", Toast.LENGTH_SHORT).show()
                    }
                }

                Constants.MESSAGE_DEVICE -> {
                    val device: BluetoothDevice = it.obj as BluetoothDevice

                    bluetoothService = BluetoothService(handler, device)
                    bluetoothService.ConnectThread().start()
                }

                Constants.MESSAGE_ERROR -> {
                    bluetoothDialog.setConnectionIconToDisconnected()
                    bluetoothDialog.dismiss()
                }
            }
            true
        }

//        bluetoothService = BluetoothService(handler)

        motorAdapter.motors = motorRealm.where(Motor::class.java).findAll()
        motorRealm.addChangeListener { motorAdapter.notifyDataSetChanged() }

        if (motorAdapter.motors.isEmpty()) {
            val motorSettingDialog = MotorSettingDialogFragment()
            motorSettingDialog.show(supportFragmentManager, "missiles")
        }

        switch_auto.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {

            } else {

            }
        }

        button_write.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (::bluetoothService.isInitialized) {
                        if (bluetoothService.isConnected()) {
                            isWriteButtonPressed = true
                            TouchHandleThread().start()
                        } else {
                            Toast.makeText(applicationContext, "블루투스를 연결해주세요", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(applicationContext, "Late init exception", Toast.LENGTH_SHORT).show()
                    }
                }

                MotionEvent.ACTION_UP -> {
                    view.performClick()
                    isWriteButtonPressed = false

                    if (::bluetoothService.isInitialized) {
                        if (bluetoothService.isConnected()) {
                            bluetoothService.IOThread().write(2)
                        }
                    }
                }
            }

            true
        }

//        motorAdapter.setOnItemClickListener(object : MotorRecyclerAdapter.OnItemClickListener {
//            override fun onItemClick(view: View, position: Int) {
//                val message = "Test message"
//                bluetoothService.IOThread(handler).write(message.toByteArray())
//            }
//        })
    }

    private fun clearMotorList() {
        Realm.getDefaultInstance().use {
            val results = it.where(Motor::class.java).findAll()

            it.executeTransaction {
                results.deleteAllFromRealm()
            }
        }
    }

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
                clearMotorList()
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

    override fun onDestroy() {
        super.onDestroy()

        motorRealm.close()

        if (::bluetoothService.isInitialized) {
            bluetoothService.ConnectThread().cancel()
        } else {
            Toast.makeText(applicationContext, "Late init exception", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class TouchHandleThread : Thread() {
        override fun run() {
            super.run()

            var count = 0
            try {
                while (isWriteButtonPressed) {
                    Log.d("Debug", "${count++}")
                    val msg = "${count++}"

                    if (::bluetoothService.isInitialized) {
                        bluetoothService.IOThread().write(1)
                    }

                    sleep(5)
                }
            } catch (e: Exception) {
                Log.d("Debug", "Touch handler thread error", e)
            }
        }
    }
}
