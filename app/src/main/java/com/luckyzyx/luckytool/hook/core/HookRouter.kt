package com.luckyzyx.luckytool.hook.core

import android.content.pm.ApplicationInfo
import android.util.Log

/**
 * 包路由：对齐迁移前 YukiEntry 的 loadApp / loadSystem。
 * 由 LibXposedEntry 的 onPackageReady / onSystemServerStarting 驱动分发。
 *
 * 每个宿主包回调都会 dispatch：该包注册的 Hooker 执行 onHook，并把本次分发上下文
 * （[PackageContext]，wrapper 语义）绑定给对应 Hooker——寄生于其它 App 的软件包
 * （如 com.oplus.keyguard.clock.base 寄生于 SystemUI）也能在共享进程内正常装载，
 * 且共享进程内后续包的 dispatch 不影响已分发 Hooker 的 packageName/classLoader。
 */
object HookRouter {

    private val appHookers = mutableMapOf<String, MutableList<Hooker>>()

    private val systemHookers = mutableListOf<Hooker>()

    /** 每 Hooker 持有的分发上下文（对齐 legacy PackageParam.wrapper） */
    private val hookerContexts = mutableMapOf<Hooker, PackageContext>()

    internal fun contextOf(hooker: Hooker): PackageContext? = hookerContexts[hooker]

    /** 对齐 legacy loadHooker 的 assignInstance：子 Hooker 继承父 Hooker 的分发上下文 */
    internal fun assignContext(hooker: Hooker, context: PackageContext?) {
        if (context != null) hookerContexts[hooker] = context
    }

    fun app(packageName: String, hooker: Hooker) {
        appHookers.getOrPut(packageName) { mutableListOf() }.add(hooker)
    }

    fun system(hooker: Hooker) {
        systemHookers += hooker
    }

    fun dispatch(packageName: String, classLoader: ClassLoader, appInfo: ApplicationInfo?) {
        Env.enter(packageName, classLoader, appInfo)
        val hookers = if (packageName == "android") {
            systemHookers + (appHookers["android"] ?: emptyList())
        } else {
            appHookers[packageName] ?: emptyList()
        }
        val context = PackageContext(packageName, classLoader, appInfo)
        hookers.forEach { hooker ->
            hookerContexts[hooker] = context
            runCatching { hooker.onHook() }
                .onFailure { Env.log(Log.ERROR, "HookRouter", "${hooker::class.java.name} failed for $packageName", it) }
        }
    }
}