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
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ConnectedDevicesAdapter

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
        adapter?.clear()
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

            val connDevices = arrayListOf<NetDevice>()
            GlobalScope.async {
                connDevices.addAll(getConnectedDevices())
            }.await()
            toast("scan stopped")
            startScanBtn.visible()
            progressBar.invisible()

            devicesList.visible()
            adapter.refreshData(connDevices)
        }
    }

    private var progressCounter = 0

    private val progressHandler = Handler(Handler.Callback { message ->
        when (message.what) {
            SHOW_PROGRESS -> {
                pingProgress.visible()
                progressCounter = 0
            }
            HIDE_PROGRESS -> pingProgress.gone()
            UPDATE_PROGRESS -> {
                progressCounter++
                pingProgress.progress = progressCounter
            }

        }
        false
    })

    private suspend fun getConnectedDevices(): ArrayList<NetDevice> {
        val myIPArray = getMyIp().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        myIPArray.reverse()
        val list = arrayListOf<NetDevice>()
        var currentPingAddr: InetAddress
        progressHandler.sendEmptyMessage(SHOW_PROGRESS)
        try {
            for ((loopCurrentIP) in (0..255).withIndex()) {
                currentPingAddr = InetAddress.getByName(
                    myIPArray[0] + "." +
                            myIPArray[1] + "." +
                            myIPArray[2] + "." +
                            loopCurrentIP.toString()
                )
                currentPingAddr.isReachable(1)
                progressHandler.sendEmptyMessage(UPDATE_PROGRESS)

//                if (currentPingAddr.isReachable(50)) {
//                    val result = server.getSystemInfoUnauthAsync(currentPingAddr.hostName)
//                    if (result.isSuccess) {
//                        list.add(currentPingAddr)
//                        val macAddr = getMacAddressFromIP(currentPingAddr.hostName)
//                        val macAddr2 = toReadPingCache()
//                        progressHandler.sendEmptyMessage(UPDATE_PROGRESS)
//                    } else {
//                        progressHandler.sendEmptyMessage(UPDATE_PROGRESS)
//                    }
//                } else {
//                    progressHandler.sendEmptyMessage(UPDATE_PROGRESS)
//                }
            }
            val exampleMac = getString(R.string.icotera_device_mac_subs)
            val macAddrs = toReadPingCache()
            for (item in macAddrs.split("\n")) {
                if (item.contains(exampleMac)) {
                    val netDevice = NetDevice(item)
                    val result = server.getSystemInfoUnauthAsync(netDevice.ipAddr)
                    if (result.isSuccess) {
                        list.add(netDevice)
                    }
                }
            }
        } catch (ex: UnknownHostException) {
        } catch (ex: IOException) {
        }
        progressHandler.sendEmptyMessage(HIDE_PROGRESS)
        return list
    }

    private fun getMyIp(): String {
        val wifiManger = getSystemService(Context.WIFI_SERVICE) as WifiManager
        val addrByteArray = BigInteger.valueOf(wifiManger.connectionInfo.ipAddress.toLong()).toByteArray()
        return InetAddress.getByAddress(addrByteArray).hostAddress
    }

    private fun checkCurrentWifi(): Boolean {
        var isOk = false

        val wifiManger = getSystemService(Context.WIFI_SERVICE) as WifiManager
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

    private fun getMacAddressFromIP(ipFinding: String): String {
        log_i("IPScanning", "Scan was started!")
        var bufferedReader: BufferedReader? = null
        try {
            bufferedReader = BufferedReader(FileReader("/proc/net/arp"))

            var line = bufferedReader.readLine()
            while (line != null) {
                val splitted = line.split(" +")
                line = bufferedReader.readLine()
                if (splitted.size >= 4) {
                    val ip = splitted[0]
                    val mac = splitted[3]
                    if (mac.matches("..:..:..:..:..:..".toRegex()))
                        if (ip.equals(ipFinding, ignoreCase = true)) return mac
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                bufferedReader!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return "00:00:00:00"
    }

    private fun getSubnetAddress(): String {
        val wManger = getSystemService(Context.WIFI_SERVICE) as WifiManager
        val address = wManger.dhcpInfo.gateway
        return String.format(
            "%d.%d.%d",
            address and 0xff,
            address shr 8 and 0xff,
            address shr 16 and 0xff
        )
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
}
