package com.luckyzyx.luckytool.hook.core

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.util.Log

/**
 * Hooker 基接口：对应迁移前 YukiBaseHooker 的常用成员面，
 * 使既有 hook 文件只需替换基类与 import。
 */
interface Hooker {

    fun onHook()

    val packageName: String get() = Env.packageName

    val processName: String get() = Env.processName

    val classLoader: ClassLoader? get() = Env.classLoader

    val appInfo: ApplicationInfo? get() = Env.appInfo

    /** 同形 prefs(ModulePrefs)：远程偏好读取 */
    fun prefs(name: String): SharedPreferences = Env.prefs(name)

    /** 同形 loadHooker：装载子 Hooker，异常隔离 */
    fun loadHooker(hooker: Hooker) {
        runCatching { hooker.onHook() }
            .onFailure { Env.log(Log.ERROR, "HookRouter", "loadHooker ${hooker::class.java.name} failed", it) }
    }
}