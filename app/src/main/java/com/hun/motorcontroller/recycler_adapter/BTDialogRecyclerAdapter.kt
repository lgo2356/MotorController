package com.hun.motorcontroller.recycler_adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hun.motorcontroller.R
import com.hun.motorcontroller.data.Device
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.layout_bluetooth_device_item.view.*

class BTDialogRecyclerAdapter(private val items: ArrayList<Device>) :
    RecyclerView.Adapter<BTDialogRecyclerAdapter.ViewHolder>() {

    private var clickListener: OnItemClickListener? = null

    private var isConnecting: Boolean = false

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.layout_bluetooth_device_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.deviceName.text = items[position].deviceName

        holder.itemView.setOnClickListener {
            clickListener?.onItemClick(it, position)
        }

        if (isConnecting) {
            holder.connectingProgressBar.visibility = View.VISIBLE
        } else {
            holder.connectingProgressBar.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.textview_device_name
        val connectingProgressBar: ProgressBar = itemView.progressbar_connecting
//        val deviceAddress: TextView = itemView.textview_device_address
//        val deviceConnectingProgress: ImageView = itemView.image_device_connecting_progress
    }

    fun addItem(name: String, address: String) {
        val item = Device(name, address)
        items.add(item)
        notifyDataSetChanged()
    }

    fun getItems(): List<Device> {
        return this.items
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.clickListener = listener
    }

    fun setConnectingProgress(isConnecting: Boolean) {
        this.isConnecting = isConnecting
        notifyDataSetChanged()
    }
}
