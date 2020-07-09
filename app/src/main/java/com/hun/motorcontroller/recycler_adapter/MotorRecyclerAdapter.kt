package com.hun.motorcontroller.recycler_adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Motor
import kotlinx.android.synthetic.main.layout_motor_control_list.view.*

class MotorRecyclerAdapter : RecyclerView.Adapter<MotorRecyclerAdapter.ViewHolder>() {

    var motors: List<Motor> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.layout_motor_control_list, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.btnMotor.text = motors[position].name
    }

    override fun getItemCount(): Int {
        return motors.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chkBox: CheckBox = itemView.chkbox_motor
        val btnMotor: Button = itemView.button_motor
    }
}
