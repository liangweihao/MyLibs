package com.nothing.commonutils.utils


import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.util.Consumer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object WifiStateListener {

    private const val TAG = "WifiStateListener"
    private var networkCallback: NetworkCallback? = null  // 新增：保存回调引用用于注销
    private val compositeDisposable = CompositeDisposable()  // 新增：统一管理Rx资源
    private val isOnline = AtomicBoolean(false)  // 新增：原子变量确保线程安全
    private val networkValidatedMap = mutableMapOf<Long, Boolean>()  // 新增：跟踪各网络验证状态

    fun startListening(context: Context, testHost: String, stateResult: Consumer<Boolean>) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 1. 初始网络状态检查（含验证状态）
        checkCurrentNetworkState(context, stateResult)

        // 2. 创建网络请求（明确需要验证过的网络）
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)  // 新增：要求已验证的网络
            .build()

        // 3. 创建可注销的NetworkCallback
        networkCallback = object : NetworkCallback() {
            // 网络可用时触发（但未必已验证）
            override fun onAvailable(network: Network) {
                Lg.i(TAG, "Network available: ${network.networkHandle}")
                networkValidatedMap[network.networkHandle] = false  // 初始标记为未验证
                checkNetworkValidation(connectivityManager, network, testHost, stateResult)
            }

            // 网络能力变化时触发（关键：监听VALIDATED状态）
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                val previousValidated = networkValidatedMap[network.networkHandle] ?: false

                if (isValidated != previousValidated) {
                    networkValidatedMap[network.networkHandle] = isValidated
                    Lg.i(TAG, "Network ${network.networkHandle} validated: $isValidated")
                    if (isValidated) {
                        checkNetworkValidation(connectivityManager, network, testHost, stateResult)
                    } else if (isOnline.get()) {
                        // 若当前在线但该网络失去验证，需重新评估整体状态
                        evaluateOverallState(connectivityManager, stateResult)
                    }
                }
            }

            // 网络丢失时触发
            override fun onLost(network: Network) {
                Lg.i(TAG, "Network lost: ${network.networkHandle}")
                networkValidatedMap.remove(network.networkHandle)
                evaluateOverallState(connectivityManager, stateResult)  // 重新评估整体状态
            }
        }

        // 4. 注册网络回调
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    // 新增：停止监听并释放资源（防止内存泄漏）
    fun stopListening(context: Context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        compositeDisposable.dispose()
        networkValidatedMap.clear()
        isOnline.set(false)
    }

    // 评估整体网络状态（存在任何已验证网络即视为在线）
    private fun evaluateOverallState(connectivityManager: ConnectivityManager, stateResult: Consumer<Boolean>) {
        // 关键：只要有一个网络已验证，即视为整体在线
        val hasAnyValidatedNetwork = networkValidatedMap.values.any { it }
        val newState = hasAnyValidatedNetwork

        if (isOnline.get() != newState) {
            isOnline.set(newState)
            stateResult.accept(newState)
            Lg.i(TAG, "Overall network state changed to: $newState (validated networks: ${networkValidatedMap.count { it.value }})")
        }
    }

    // 检查网络实际连通性（绑定特定网络+HEAD请求优化）
    // 检查网络实际连通性（增加重试机制）
    private fun checkNetworkValidation(
        connectivityManager: ConnectivityManager,
        network: Network,
        testHost: String,
        stateResult: Consumer<Boolean>
    ) {
        // 取消之前的验证任务
        compositeDisposable.clear()

        // 新增：3次重试，间隔2秒（适应新网络初期不稳定）
        val MAX_RETRIES = 3
        val validationDisposable = Observable.range(0, MAX_RETRIES)
            .delay { index -> Observable.timer(index * 2L, TimeUnit.SECONDS) } // 0s, 2s, 4s重试
            .flatMap { attempt ->
                Observable.create<Boolean> { emitter ->
                    try {
                        val url = URL(testHost)
                        val connection = network.openConnection(url) as HttpURLConnection
                        connection.requestMethod = "HEAD"
                        connection.connectTimeout = 5000
                        connection.readTimeout = 5000
                        connection.connect()
                        val responseCode = connection.responseCode
                        val success = responseCode in 200..299
                        emitter.onNext(success)
                        Lg.i(TAG, "Network ${network.networkHandle} validation attempt $attempt: ${if (success) "success" else "failed"}")
                    } catch (e: IOException) {
                        emitter.onNext(false)
                    }
                    emitter.onComplete()
                }.subscribeOn(Schedulers.io())
            }
            .firstOrError() // 取首次成功，若无则失败
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ success ->
                if (success) {
                    networkValidatedMap[network.networkHandle] = true
                    evaluateOverallState(connectivityManager, stateResult)
                } else {
                    Lg.w(TAG, "Network ${network.networkHandle} failed after $MAX_RETRIES attempts")
                }
            }, { e ->
                Lg.e(TAG, "Network validation error", e)
            })

        compositeDisposable.add(validationDisposable)
    }

    // 初始网络状态检查（含验证状态）
    private fun checkCurrentNetworkState(context: Context, stateResult: Consumer<Boolean>) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: run {
            stateResult.accept(false)
            return
        }

        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isValidated = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        isOnline.set(isValidated)
        stateResult.accept(isValidated)
        Lg.i(TAG, "Initial network state: ${if (isValidated) "online" else "offline"}")
    }
}
