package com.luckyzyx.luckytool.ui.service

import android.content.SharedPreferences
import com.luckyzyx.luckytool.ui.service.LxServiceBridge.preferences
import com.luckyzyx.luckytool.utils.LogUtils
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 模块 App 侧接入 libxposed service 的唯一入口：
 * - 绑定成功后持有 [XposedService]
 * - [preferences] 返回 remote prefs（宿主进程 Env.prefs 的同一数据源），
 *   绑定不可用（未激活/无框架）时回落 null，由调用方走本地 prefs
 */
object LxServiceBridge {

    private val bindSignal = CountDownLatch(1)

    @Volatile
    private var initialized = false

    @Volatile
    var xposedService: XposedService? = null
        private set

    fun init() {
        if (initialized) return
        initialized = true
        XposedServiceHelper.registerListener(object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                xposedService = service
                bindSignal.countDown()
                LogUtils.d("LxServiceBridge", "bind service", "API ${service.apiVersion}", true)
            }

            override fun onServiceDied(service: XposedService) {
                xposedService = null
            }
        })
    }

    /** 等待服务绑定完成（无框架环境超时后保持 null） */
    fun awaitReady(timeoutMs: Long = 3000) {
        runCatching { bindSignal.await(timeoutMs, TimeUnit.MILLISECONDS) }
    }

    /** 模块是否处于激活态：框架已绑定且 scope 列表非空 */
    val isModuleActive: Boolean
        get() = xposedService?.scope?.isNotEmpty() == true

    fun preferences(name: String): SharedPreferences? =
        xposedService?.getRemotePreferences(name)
}