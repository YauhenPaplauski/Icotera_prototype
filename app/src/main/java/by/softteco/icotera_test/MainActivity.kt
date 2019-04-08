package by.softteco.icotera_test

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import by.softteco.icotera_test.adapter.ConnectedDevicesAdapter
import by.softteco.icotera_test.models.NetDevice
import by.softteco.icotera_test.utils.*
import by.softteco.icotera_test.utils.Constants.BEFORE_API_QUERY_STARTED
import by.softteco.icotera_test.utils.Constants.BEFORE_PING_STARTED
import by.softteco.icotera_test.utils.Constants.HIDE_PROGRESS
import by.softteco.icotera_test.utils.Constants.SHOW_PROGRESS
import by.softteco.icotera_test.utils.Constants.UPDATE_API_QUERY_PROGRESS
import by.softteco.icotera_test.utils.Constants.UPDATE_PING_PROGRESS
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.IOException
import java.math.BigInteger
import java.net.InetAddress
import java.net.NetworkInterface
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
        if (isIcoteraDeviceNetwork()) scanNetwork()
    }

    // dispatches execution into Android main thread
    val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    // represent a pool of shared threads as coroutine dispatcher
    val bgDispatcher: CoroutineDispatcher = Dispatchers.IO

    val uiScope = CoroutineScope(Dispatchers.Main)

    private fun scanNetwork() {
        uiScope.launch {
            adapter.clear()
            scanStarted()
            GlobalScope.async {
                discoverIcoteraDevices()
            }.await()
            scanStopped()
        }
    }

    private fun scanStarted() {
        devicesList.gone()
        progressBar.visible()
        infoBox.visible()
        startScanBtn.invisible()
        toast("scan started")
        progressHandler.sendEmptyMessage(SHOW_PROGRESS)
    }

    private fun scanStopped() {
        progressHandler.sendEmptyMessageDelayed(HIDE_PROGRESS, 300)
        toast("scan stopped")
        startScanBtn.visible()
        progressBar.invisible()
        devicesList.visible()
        infoBox.gone()
    }

    private val progressHandler = Handler(Handler.Callback { message ->
        when (message.what) {
            SHOW_PROGRESS -> {
                pingProgress.visible()
            }
            HIDE_PROGRESS -> pingProgress.gone()
            BEFORE_PING_STARTED -> {
                progressCounter = 0
                pingProgress.max = (networkEnd - networkStart).toInt()
                processInfoText.text = ""
            }
            UPDATE_PING_PROGRESS -> {
                progressCounter++
                pingProgress.progress = progressCounter
                processInfoText.text = message.obj.toString()
                //TODO update list in realtime  if (adapter.itemCount > 0) adapter.notifyDataSetChanged()
            }
            BEFORE_API_QUERY_STARTED -> {
                progressCounter = 0
                pingProgress.progress = 0
                pingProgress.max = message.arg1
                processInfoText.text = getString(R.string.querying_devices)
            }
            UPDATE_API_QUERY_PROGRESS -> {
                progressCounter++
                pingProgress.progress = progressCounter
            }
        }
        false
    })

    private suspend fun discoverIcoteraDevices() {
//        possible
//        10.0.0.0/8
//        172.16.0.0/12
//        192.168.0.0/16
        calculateRange()
        pingAllIpInRange()
        parsePingCache()

//        try {
//        } catch (ex: UnknownHostException) {
//        } catch (ex: IOException) {
//        }
    }

    private fun pingAllIpInRange() {
        progressHandler.sendEmptyMessage(BEFORE_PING_STARTED)
        for (addr in networkEnd downTo networkStart) {
            val i = InetAddress.getByAddress(BigInteger.valueOf(addr).toByteArray())
            i.isReachable(10)

            val msg = Message()
            msg.what = UPDATE_PING_PROGRESS
            msg.obj = i.toString()
            progressHandler.sendMessage(msg)
        }
    }

    private suspend fun parsePingCache() {
        val cache = readPingCache()
        initBeforePing(cache.size)
        for (item in cache) {
            val netDevice = NetDevice(item)
            val result = api.getSystemInfoUnauthAsync(netDevice.ipAddr)
            if (result.isSuccess) {
                adapter.addDevice(netDevice)
                progressHandler.sendEmptyMessage(UPDATE_API_QUERY_PROGRESS)
            }
        }
    }

    private fun initBeforePing(size: Int) {
        val msg = Message()
        msg.arg1 = size
        msg.what = BEFORE_API_QUERY_STARTED
        progressHandler.sendMessage(msg)
    }

    private fun getGateway(): String {
        return intToIp(getWManager().dhcpInfo.gateway)
    }

    private fun getNetMask(): String {
        return intToIp(getWManager().dhcpInfo.netmask)
    }

    private fun getMyIpAddress(): String {
        return intToIp(getWManager().connectionInfo.ipAddress)
    }

    private fun getIpAddress(): Long {
        return getWManager().connectionInfo.ipAddress.toLong()
    }

    private fun getWManager(): WifiManager {
        return applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
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

    private fun isIcoteraDeviceNetwork(): Boolean {
        val wifiManger = getWManager()
        if (wifiManger.isWifiEnabled) {
            val wInfo = wifiManger.connectionInfo
            if (wInfo.bssid.startsWith(getString(R.string.icotera_device_mac_subs)))
                return true
            else
                toast(getString(R.string.select_icotera_wifi))
        } else
            toast(getString(R.string.connect_to_wifi))
        return false
    }

    private fun readPingCache(): List<String> {
        val icoteraMacExample = getString(R.string.icotera_device_mac_subs)
        val args = listOf("cat", "/proc/net/arp")
        val cmd: ProcessBuilder
        var result = ""

        try {
            cmd = ProcessBuilder(args)

            val process = cmd.start()
            val `in` = process.inputStream
            val re = ByteArray(1024)
            while (`in`.read(re) !== -1) {
                if (String(re).contains(icoteraMacExample)) {
                    println(String(re))
                    result += String(re)
                }
            }
            `in`.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return result.split("\n").filter { it.contains(icoteraMacExample) }
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
