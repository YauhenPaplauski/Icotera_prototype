package by.softteco.icotera_test.utils

import by.softteco.icotera_test.models.GetSystemInfoUnathorized
import by.softteco.icotera_test.utils.Result

interface IcoteraApi {
    suspend fun getSystemInfoUnauthAsync(targetHost: String): Result<GetSystemInfoUnathorized>
}