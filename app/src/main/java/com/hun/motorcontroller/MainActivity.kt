package com.hun.motorcontroller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.GridLayoutManager
import com.hun.motorcontroller.data.BTSocket
import com.hun.motorcontroller.data.Motor
import com.hun.motorcontroller.dialog.BluetoothDialogFragment
import com.hun.motorcontroller.dialog.MotorSettingDialogFragment
import com.hun.motorcontroller.recycler_adapter.MotorRecyclerAdapter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

import com.hun.motorcontroller.BluetoothService
import java.io.IOException
import java.lang.Exception
import java.nio.charset.StandardCharsets.US_ASCII

class MainActivity : AppCompatActivity() {

    private var isWriteButtonPressed = false

    private val motorAdapter: MotorRecyclerAdapter = MotorRecyclerAdapter()
    private val motorRealm: Realm = Realm.getDefaultInstance()
    private var readDisposable: Disposable? = null

    private val bluetoothService = BluetoothService()
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
                    text_message.text = readString
                }

                Constants.MESSAGE_TOAST -> {
                    val message = it.obj.toString()
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                }

                Constants.MESSAGE_CONNECTED -> {
                    Toast.makeText(applicationContext, "데이터 수신 시작", Toast.LENGTH_SHORT).show()
                    bluetoothService.IOThread(handler).start()
                }
            }
            true
        }

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
                    isWriteButtonPressed = true
                    TouchHandleThread().start()
                }

                MotionEvent.ACTION_UP -> {
                    view.performClick()
                    isWriteButtonPressed = false
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
                val bluetoothDialog = BluetoothDialogFragment(handler)
                bluetoothDialog.show(supportFragmentManager, "missiles")
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

        try {
            BTSocket.socket?.close()
        } catch (e: Exception) {
            Log.d("Debug", "Couldn't close your socket", e)
        }
    }

    private inner class TouchHandleThread : Thread() {
        override fun run() {
            super.run()

            var count = 0
            while (isWriteButtonPressed) {
                Log.d("Debug", "${count++}")
                val msg = "${count++}"
                bluetoothService.IOThread(handler).write(msg.toByteArray())
                sleep(100)
            }
        }
    }
}
