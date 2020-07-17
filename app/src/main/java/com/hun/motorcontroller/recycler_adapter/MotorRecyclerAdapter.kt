package com.hun.motorcontroller.recycler_adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Motor
import io.realm.Realm

class MotorRecyclerAdapter : RecyclerView.Adapter<MotorRecyclerAdapter.ViewHolder>() {

    var motors: List<Motor> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.layout_motor_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textMotorName.text = motors[position].name

        holder.itemView.setOnClickListener {
            listener?.onItemClick(it, position)
        }

        holder.imageBin.setOnClickListener {
            deleteMotor(position)
        }
    }

    override fun getItemCount(): Int {
        return motors.size
    }

    private fun deleteMotor(position: Int) {
        Realm.getDefaultInstance().use {
            val foundMotor: Motor? = it.where(Motor::class.java).findAll()[position]

            it.executeTransaction {
                foundMotor?.deleteFromRealm()
            }
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMotorName: TextView = itemView.findViewById(R.id.text_motor_name)
        val imageBin: ImageView = itemView.findViewById(R.id.image_bin)
    }
}
