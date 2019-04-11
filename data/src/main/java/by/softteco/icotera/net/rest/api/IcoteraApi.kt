package by.softteco.icotera.net.rest.api

import by.softteco.icotera.net.entity.CfgInfoUnath
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

interface IcoteraApi {
    @Headers(
        "Accept-Encoding: gzip, deflate",
        "Accept-Charset: utf-8",
        "Content-Type: text/plain",
        "Content-Length: 0"
    )
    @GET
    fun getSystemInfoUnauthAsync(@Url url: String): Observable<CfgInfoUnath>
}