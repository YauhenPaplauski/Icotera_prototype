package by.softteco.icotera.net.rest.service

import by.softteco.icotera.net.entity.CfgInfoUnath
import by.softteco.icotera.net.rest.api.IcoteraApi
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RestService {
    val icoteraApi: IcoteraApi

    init {
        val okHttpBuilder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)

        okHttpBuilder.addInterceptor(HttpLoggingInterceptor())

        val gson = GsonBuilder()
            .create()

        val retrofit = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpBuilder.build())
            .build()

        icoteraApi = retrofit.create(IcoteraApi::class.java)
    }

    fun getCfgInfoUnauth(url: String): Observable<CfgInfoUnath> {
        return icoteraApi.getSystemInfoUnauthAsync(url)
    }
}