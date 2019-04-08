package by.softteco.icotera_test.utils

import by.softteco.icotera_test.models.CfgInfoUnath

interface IcoteraApi {
    suspend fun getCfgInfoUnauth(targetHost: String): Result<CfgInfoUnath>
}