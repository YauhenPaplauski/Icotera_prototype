package by.softteco.icotera_test.models

data class GetSystemInfoUnathorized(
    val timestamp: Long,
    val magic: String,
    val trlock: Int,
    val block: Int,
    val curpg: String,
    val logged: Int,
    val changes: Int,
    val dbsync: Int,
    val authtype: Int,
    val idx_lang: Int,
    val resp_data: Any,
    val EoO: String
)