package by.softteco.icotera_test.models

data class CfgInfoUnath(
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
    val sid: String,
    val resp_data: ResponseData,
    val EoO: String
)