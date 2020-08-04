package com.hun.motorcontroller.dialog

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Motor
import com.hun.motorcontroller.recycler_adapter.MotorRecyclerAdapter
import io.realm.Realm
import java.lang.IllegalStateException

class MotorSettingDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { fragmentActivity ->
            val builder = AlertDialog.Builder(fragmentActivity)
            val inflater = requireActivity().layoutInflater
            val view: View = inflater.inflate(R.layout.layout_motor_number_dialog, null)
            val motorCountEditText = view.findViewById<EditText>(R.id.edit_motor_count)

            builder.setView(view)
                .setTitle("모터 개수 입력")
                .setPositiveButton("확인") { _, _ ->
                    val newMotorCount: Int = motorCountEditText.text.toString().toInt()
                    val motorCount: Long = getMotorCount()
                    for (i in (motorCount+1)..(motorCount+newMotorCount)) {
                        setMotorName("Motor")
                    }
                }
                .setNegativeButton("취소") { _, _ -> }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
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

    private fun getMotorCount(): Long {
        var motorCount: Long = 0
        Realm.getDefaultInstance().use {
            motorCount = it.where(Motor::class.java).count()
        }
        return motorCount
    }

//    private fun showNameSetDialog(fragmentActivity: FragmentActivity, count: Int) {
//        MotorNameDialogFragment().apply {
//            val args = Bundle()
//            args.putInt("count", count)
//            this.arguments = args
//            this.show(fragmentActivity.supportFragmentManager, "missiles")
//        }
//    }
}
