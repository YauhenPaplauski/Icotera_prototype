package by.softteco.icotera_test.utils

import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.EOFException
import java.net.CookieManager
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

inline fun <reified T> create(): T {
    val httpClientBuilder = OkHttpClient.Builder()
    val cookieHandler = CookieManager()

    httpClientBuilder
        .cookieJar(JavaNetCookieJar(cookieHandler))
        .readTimeout(1, TimeUnit.SECONDS)
        .connectTimeout(1, TimeUnit.SECONDS)

    onlyDebugConsume {
        val loggingInterceptor = HttpLoggingInterceptor { l -> log_d("HTTP", l) }
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        httpClientBuilder.addInterceptor(loggingInterceptor)
    }

    val httpClient = httpClientBuilder.build()

    val retrofit = Retrofit.Builder()
        .baseUrl("http://google.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

    return retrofit.create(T::class.java)
}

val api: IcoteraApi
    get() = Services.icoteraApi

data class Result<out T>(val data: T? = null, val error: Error? = null) {
    val isSuccess: Boolean
        get() = data != null
}

suspend fun <T> Call<T>.executeAsync(): T = suspendCancellableCoroutine { continuation ->
    this.enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>?, t: Throwable) {
            if (t is EOFException) continuation.resume("" as T)
            else
                continuation.resumeWithException(t)
        }

        override fun onResponse(call: Call<T>?, response: Response<T>) {

            if (response.isSuccessful) {
                if (response.code() == 200) {
                    if (response.body() == null) {
                        continuation.resume("" as T)
                    } else
                        continuation.resume(response.body()!!)
                } else
                //if (response.code() == 201){
                //    continuation.resume(response.headers().get("Location") as T)
                //}else
                //response.headers().toString()
                    continuation.resume("" as T)
                //continuation.resume(response.body() as T)

            } else {
                continuation.resumeWithException(retrofit2.HttpException(response))
            }
        }
    })
    continuation.invokeOnCancellation {
        if (continuation.isCancelled) {
            try {
                cancel()
            } catch (e: Throwable) {
            }
        }
    }
}

sealed class Error {
    //Server
    data class UnknownError(val code: Int = 1000, val msg: String = "Unknown error") : Error()

    data class NetworkError(val code: Int = 1001, val msg: String = "Network error") : Error()
    data class DataParsingError(val code: Int = 1002, val msg: String = "Data parsing error") : Error()
    data class AuthError(val code: Int = 1003, val msg: String = "Autorization error") : Error()

    data class HttpError(val code: Int, val msg: String) : Error()

    //Local Database
    data class DatabaseExecuteError(val code: Int = 2000, val msg: String = "Database execute error") : Error()
}

suspend fun <T> Call<T>.executeAsyncAndHandleError(): Result<T> = try {
    Result(executeAsync(), null)
} catch (e: UnknownHostException) {
    Result(null, Error.NetworkError())
} catch (e: SocketTimeoutException) {
    Result(null, Error.NetworkError())
} catch (e: HttpException) {
    Result(null, Error.HttpError(e.code(), e.message()))
} catch (e: MalformedJsonException) {
    Result(null, Error.DataParsingError())
} catch (e: Throwable) {
    Result(null, Error.UnknownError())
}