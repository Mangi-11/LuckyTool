package com.luckyzyx.luckytool.hook.core

import android.content.pm.ApplicationInfo
import android.util.Log

/**
 * 包路由：对齐迁移前 YukiEntry 的 loadApp / loadSystem。
 * 由 LibXposedEntry 的 onPackageReady / onSystemServerStarting 驱动分发。
 */
object HookRouter {

    private val appHookers = mutableMapOf<String, MutableList<Hooker>>()

    private val systemHookers = mutableListOf<Hooker>()

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
        hookers.forEach { hooker ->
            runCatching { hooker.onHook() }
                .onFailure { Env.log(Log.ERROR, "HookRouter", "${hooker::class.java.name} failed for $packageName", it) }
        }
    }
}