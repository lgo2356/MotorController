package com.hun.motorcontroller.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hun.motorcontroller.MainActivity
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Motor
import com.hun.motorcontroller.recycler_adapter.MotorNameRecyclerAdapter
import io.realm.Realm

class MotorNameDialogFragment : DialogFragment() {

    private val motors = ArrayList<Motor>()
    private val motorNameRecyclerAdapter = MotorNameRecyclerAdapter(motors)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { fragmentActivity ->
            val builder = AlertDialog.Builder(fragmentActivity)
            val inflater = requireActivity().layoutInflater
            val view: View = inflater.inflate(R.layout.layout_motor_name_dialog, null)
            val motorNameRecycler = view.findViewById<RecyclerView>(R.id.recycler_motor_name)

            motorNameRecycler.adapter = motorNameRecyclerAdapter
            motorNameRecycler.layoutManager = LinearLayoutManager(context)

            val count: Int = arguments?.getInt("count") ?: 0
            // Default name
            for (i in 0 until count) motorNameRecyclerAdapter.addItem("Motor ${i + 1}")

            builder
                .setView(view)
                .setTitle("모터 이름 설정")
                .setPositiveButton("확인") { _, _ ->
                    for (i in 0 until count) {
                        val motorName = motorNameRecyclerAdapter.getName(i)
                        setMotorName(motorName)
                    }
//                    (fragmentActivity as MainActivity).getMotorAdapter()?.apply {
//                        for (i in 0 until count) {
//                            val motorName = motorNameRecyclerAdapter.getName(i)
//                            setMotorName(motorName)
////                            motorName?.let { name ->
////                                if (name.isNotEmpty()) this.addItem(name)
////                                else this.addItem("Empty Name")
////                            } ?: this.addItem("Empty Name")
//                        }
//                    }
                }
                .setNegativeButton("취소") { _, _ -> }
                .create()
        } ?: throw IllegalAccessException("Activity cannot be null")
    }

    private fun setMotorName(name: String) {
        Realm.getDefaultInstance().use {
            it.executeTransaction { realm ->
                val motor = Motor()
                val maxId: Long? = realm.where(Motor::class.java).max("id") as Long?
                motor.id = maxId?.plus(1) ?: 0

                if (name.isNotEmpty()) motor.name = name
                else motor.name = "Empty"

                realm.copyToRealm(motor)
            }
        }
    }
}