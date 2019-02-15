package by.softteco.icotera_test.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import by.softteco.icotera_test.R
import by.softteco.icotera_test.databinding.ConnectedDeviceItemBinding
import by.softteco.icotera_test.models.NetDevice

class ConnectedDevicesAdapter : RecyclerView.Adapter<ConnectedDevicesAdapter.DeviceVH>() {
    private var list = arrayListOf<NetDevice>()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): DeviceVH {
        val databinding =
            DataBindingUtil.inflate<ConnectedDeviceItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.connected_device_item,
                parent,
                false
            )
        return DeviceVH(databinding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: DeviceVH, pos: Int) {
        holder.bind(list[pos])
    }

    fun refreshData(connDevices: ArrayList<NetDevice>) {
        list.clear()
        list.addAll(connDevices)
        notifyDataSetChanged()
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    fun addDevice(netDevice: NetDevice) {
        list.add(netDevice)
    }

    inner class DeviceVH(private val binding: ConnectedDeviceItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: NetDevice) {
            binding.device = device
            binding.executePendingBindings()
        }

    }
}
