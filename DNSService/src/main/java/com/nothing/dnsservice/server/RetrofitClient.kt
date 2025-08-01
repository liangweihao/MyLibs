package com.nothing.dnsservice.server


import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nothing.commonutils.utils.Lg
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val gson: Gson = GsonBuilder().setLenient().create()

    private const val TAG = "RetrofitClient"
    fun getServerAPI(httpUrl: HttpUrl): IServerAPI {
        val httpLoggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger{
            override fun log(message: String) {
                Lg.i(TAG,"$message")
            }
        })
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        Platform.resetForTests(object : Platform() {
            override fun log(message: String, level: Int, t: Throwable?) {
                Lg.i(TAG,"message:$message")
                if (t != null) {
                    Lg.e(TAG, " Fail:${Lg.getStackTraceAsString(t)}")
                }
            }
        })
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
        val retrofit = Retrofit.Builder().baseUrl(httpUrl)
            .client(okHttpClient) // 设置 OkHttpClient
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        return retrofit.create(IServerAPI::class.java)
    }


}
