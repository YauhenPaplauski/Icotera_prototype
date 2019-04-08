package by.softteco.icotera_test.utils

import by.softteco.icotera_test.models.CfgInfoUnath
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

class IcoteraApiImpl : IcoteraApi {
    private val service: IcoteraRestService by lazy { create<IcoteraRestService>() }

    override suspend fun getSystemInfoUnauthAsync(targetHost: String): Result<CfgInfoUnath> {
        val url = "http://$targetHost/index.cgi?mode=app10&req=get_cfginfo_unauth"
        return service.getSystemInfoUnauthAsync(url).executeAsyncAndHandleError()
    }

    private interface IcoteraRestService {
        @Headers(
            "Accept-Encoding: gzip, deflate",
            "Accept-Charset: utf-8",
            "Content-Type: text/plain",
            "Content-Length: 0"
        )
        @GET
        fun getSystemInfoUnauthAsync(@Url url: String): Call<CfgInfoUnath>
    }
}