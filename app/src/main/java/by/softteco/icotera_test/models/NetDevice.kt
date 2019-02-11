package by.softteco.icotera_test.models

class NetDevice(line: String) {
    var ipAddr: String = ""
    var hwAddr: String = ""
//    var hwType: String
//    var flags: String
//    var mask: String
//    var device: String

    private val macAddrRegex = "..:..:..:..:..:..".toRegex()
    val IPADDRESS_PATTERN =
        "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
    private val ipAddrRegex = IPADDRESS_PATTERN.toRegex()

    init {
        macAddrRegex.find(line)?.let {
            hwAddr = it.value
        }
        ipAddrRegex.find(line)?.value?.let {
            ipAddr = it
        }
    }
}