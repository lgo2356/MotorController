package com.hun.motorcontroller.recycler_adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Motor
import kotlinx.android.synthetic.main.layout_motor_name_item.view.*

class MotorNameRecyclerAdapter(private val items: ArrayList<Motor>) :
    RecyclerView.Adapter<MotorNameRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.layout_motor_name_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Default Name
        holder.editMotorName.setText(items[position].name)

        holder.editMotorName.addTextChangedListener {
            items[position].name = holder.editMotorName.text.toString()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val editMotorName: EditText = itemView.edit_motor_name
    }

    fun addItem(name: String) {
        val item = Motor()
        item.name = name
        items.add(item)
    }

    fun getName(position: Int): String {
        return items[position].name
    }
}