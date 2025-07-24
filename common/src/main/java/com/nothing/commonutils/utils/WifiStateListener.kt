package com.nothing.commonutils.utils


import android.app.Application.CONNECTIVITY_SERVICE
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.util.Consumer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

object WifiStateListener {

    private const val TAG = "WifiStateListener"
    var testOnlineDisposable: Disposable? = null

    fun startListening(context: Context, testHost: String, stateResult: Consumer<Boolean>) {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        // 手动检查当前网络状态
        checkCurrentNetworkState(context, stateResult)

        val networkRequest =
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    // 网络可用
                    val MAX_RETRIES = 5// 最大重试次数
                    val RETRY_DELAY = 3 // 每次重试的延迟时间（秒）
                    Lg.i(TAG, "net state true , $network")

                    if (testOnlineDisposable != null) {
                        testOnlineDisposable?.dispose()
                    }
                    testOnlineDisposable = Observable.intervalRange(
                        0,
                        MAX_RETRIES.toLong(),
                        0,
                        RETRY_DELAY.toLong(),
                        TimeUnit.SECONDS
                    )
                        .flatMapSingle<Boolean> { index: Long? ->
                            Observable.create<Boolean> { emitter ->
                                try {
                                    val url = URL(testHost)
                                    val urlConnection =
                                        url.openConnection() as HttpURLConnection
                                    urlConnection.connectTimeout = 3000 // 超时时间为 3 秒
                                    urlConnection.connect()
                                    val responseCode = urlConnection.responseCode
                                    emitter.onNext(responseCode == HttpURLConnection.HTTP_OK)
                                } catch (e: IOException) {
                                    emitter.onNext(false)
                                }
                                emitter.onComplete()
                            }.subscribeOn(Schedulers.io())
                                .firstOrError()
                        }
                        .filter { isAvailable: Boolean? -> isAvailable!! }
                        .firstElement()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ isAvailable: Boolean ->
                            if (isAvailable) {
                                Lg.i(TAG, "Send WIFI Complete Broadcast")
                                stateResult.accept(true)
                            }
                        }, { throwable: Throwable? ->
                            Lg.e(TAG, "Failed to connect to the network after retries", throwable)
                        })

                }

                override fun onLost(network: Network) {
                    // 网络丢失
                    Lg.i(TAG, "net state onLost , $network")
                    if (testOnlineDisposable != null) {
                        testOnlineDisposable?.dispose()
                    }
                    stateResult.accept(false)
                }
            }
        )
    }

    private fun checkCurrentNetworkState(
        context: Context,
        stateResult: Consumer<Boolean>
    ) {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isConnected = capabilities != null &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        if (!isConnected) {
            stateResult.accept(false)
        }
    }
}


