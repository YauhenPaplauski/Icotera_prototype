package by.softteco.icotera_test

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import by.softteco.icotera_test.adapter.ConnectedDevicesAdapter
import by.softteco.icotera_test.models.NetDevice
import by.softteco.icotera_test.utils.*
import by.softteco.icotera_test.utils.Constants.HIDE_PROGRESS
import by.softteco.icotera_test.utils.Constants.SHOW_PROGRESS
import by.softteco.icotera_test.utils.Constants.UPDATE_PROGRESS
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.IOException
import java.math.BigInteger
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import kotlin.experimental.and

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ConnectedDevicesAdapter
    private var networkIp: Long = 0
    private var networkStart: Long = 0
    private var networkEnd: Long = 0
    private var cidr: Short = 0
    private var progressCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startScanBtn.setOnClickListener { onStartScanClicked() }
        initRecycler()
    }

    private fun initRecycler() {
        adapter = ConnectedDevicesAdapter()
        devicesList.layoutManager = LinearLayoutManager(this)
        devicesList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        devicesList.adapter = adapter
    }

    private fun onStartScanClicked() {
        adapter.clear()
        devicesList.gone()
        if (checkCurrentWifi()) scanNetwork()
    }

    // dispatches execution into Android main thread
    val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    // represent a pool of shared threads as coroutine dispatcher
    val bgDispatcher: CoroutineDispatcher = Dispatchers.IO

    val uiScope = CoroutineScope(Dispatchers.Main)

    private fun scanNetwork() {
        uiScope.launch {
            progressBar.visible()
            startScanBtn.invisible()
            toast("scan started")
            GlobalScope.async {
                pingNetwork()
            }.await()
            toast("scan stopped")
            startScanBtn.visible()
            progressBar.invisible()
            devicesList.visible()
        }
    }

    private val progressHandler = Handler(Handler.Callback { message ->
        when (message.what) {
            SHOW_PROGRESS -> {
                pingProgress.visible()
                progressCounter = 0
                pingProgress.max = (networkEnd - networkStart).toInt()
            }
            HIDE_PROGRESS -> pingProgress.gone()
            UPDATE_PROGRESS -> {
                progressCounter++
                pingProgress.progress = progressCounter
                if (adapter.itemCount > 0) adapter.notifyDataSetChanged()
            }

        }
        false
    })

    private suspend fun pingNetwork() {
//        10.0.0.0/8
//        172.16.0.0/12
//        192.168.0.0/16
        calculateRange()
        progressHandler.sendEmptyMessage(SHOW_PROGRESS)
        try {
            for (addr in networkEnd downTo networkStart) {
                val i = InetAddress.getByAddress(BigInteger.valueOf(addr).toByteArray())
                i.isReachable(10)
                progressHandler.sendEmptyMessage(UPDATE_PROGRESS)
            }

            val exampleMac = getString(R.string.icotera_device_mac_subs)
            val macAddrs = toReadPingCache()
            for (item in macAddrs.split("\n")) {
                if (item.contains(exampleMac)) {
                    val netDevice = NetDevice(item)
                    val result = api.getSystemInfoUnauthAsync(netDevice.ipAddr)
                    if (result.isSuccess) {
                        adapter.addDevice(netDevice)
                        progressHandler.sendEmptyMessage(UPDATE_PROGRESS)
                    }
                }
            }
        } catch (ex: UnknownHostException) {
        } catch (ex: IOException) {
        }
        progressHandler.sendEmptyMessage(HIDE_PROGRESS)
    }

    private fun getGateway(): String {
        val wManger = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return intToIp(wManger.dhcpInfo.gateway)
    }

    private fun getNetMask(): String {
        val wManger = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return intToIp(wManger.dhcpInfo.netmask)
    }

    private fun getMyIpAddress(): String {
        val wifiMan = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return intToIp(wifiMan.connectionInfo.ipAddress)
    }

    private fun getIpAddress(): Long {
        val wifiMan = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiMan.connectionInfo.ipAddress.toLong()
    }

    private fun intToIp(addr: Int): String {
        return String.format(
            "%d.%d.%d.%d",
            addr and 0xff,
            addr shr 8 and 0xff,
            addr shr 16 and 0xff,
            addr shr 24 and 0xff
        )
    }

    private fun checkCurrentWifi(): Boolean {
        var isOk = false
        val wifiManger = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManger.isWifiEnabled) {
            val wInfo = wifiManger.connectionInfo
            if (wInfo.bssid.startsWith(getString(R.string.icotera_device_mac_subs)))
                isOk = true
            else
                toast(getString(R.string.select_icotera_wifi))
        } else {
            toast(getString(R.string.connect_to_wifi))
        }
        return isOk
    }

    private fun toReadPingCache(): String {
        val args = listOf("cat", "/proc/net/arp")
        val cmd: ProcessBuilder
        var result = ""

        try {
            cmd = ProcessBuilder(args)

            val process = cmd.start()
            val `in` = process.inputStream
            val re = ByteArray(1024)
            while (`in`.read(re) !== -1) {
                println(String(re))
                result = result + String(re)
            }
            `in`.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return result
    }

    private fun convertNetmaskToCIDR(maskIp: String): Int {
        //  !!! dhcpInfo.netmask is unavailable on android 6, on android 8 all OK
        val netmask = InetAddress.getByName(maskIp)
        val netmaskBytes = netmask.address
        var cidr = 0
        var zero = false
        for (b in netmaskBytes) {
            var mask = 0x80
            for (i in 0..7) {
                val result = b and mask.toByte()
                when {
                    result == 0.toByte() -> zero = true
                    zero -> throw IllegalArgumentException("Invalid netmask.")
                    else -> cidr++
                }
                mask = mask ushr 1
            }
        }
        return cidr
    }

    private fun getNetworkPrefix(ip: String): Short {
        val ipAddress = InetAddress.getByName(ip)
        val networkInterface = NetworkInterface.getByInetAddress(ipAddress)
        var netPrefix: Short = 0
        for (address in networkInterface.interfaceAddresses) {
            netPrefix = address.networkPrefixLength
        }
        return netPrefix
    }

    private fun calculateRange() {
        val ipName = getMyIpAddress()
        networkIp = getUnsignedLongFromIp(ipName)
        cidr = getNetworkPrefix(ipName)
        val shift = 32 - cidr
        if (cidr < 31) {
            networkStart = (networkIp shr shift shl shift) + 1
            networkEnd = (networkStart or ((1 shl shift) - 1).toLong()) - 1
        } else {
            networkStart = networkIp shr shift shl shift
            networkEnd = networkStart or ((1 shl shift) - 1).toLong()
        }
    }

    private fun getUnsignedLongFromIp(ip_addr: String): Long {
        val a = ip_addr.split(".")
        return (Integer.parseInt(a[0]) * 16777216
                + Integer.parseInt(a[1]) * 65536
                + Integer.parseInt(a[2]) * 256
                + Integer.parseInt(a[3])).toLong()
    }


    //    private fun getSubnetDevices() {
//        val i = ""
//        val ip = getMyIp()
//        val ip2 = getMyIpAddress()
//        progressBar.visible()
//        startScanBtn.invisible()
//        toast("scan started")
//        val list = arrayListOf<Device>()
//        SubnetDevices.fromLocalAddress().findDevices(object : SubnetDevices.OnSubnetDeviceFound {
//            override fun onFinished(devicesFound: ArrayList<Device>?) {
//                checkAccessibilityOfDevices(list)
//            }
//
//            override fun onDeviceFound(device: Device?) {
//                device?.let {
//                    if (it.mac !== null && it.mac.startsWith(getString(R.string.icotera_device_mac_subs)))
//                        list.add(it)
//                }
//            }
//        })
//    }

//    private fun checkAccessibilityOfDevices(foundDevices: ArrayList<Device>) {
//        uiScope.launch {
//            toast("scan stopped")
//            startScanBtn.visible()
//            progressBar.invisible()
//
//            devicesList.visible()
//            adapter.refreshData(foundDevices)
//        }
//    }
}
