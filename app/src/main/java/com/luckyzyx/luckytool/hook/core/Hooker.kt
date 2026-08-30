package com.luckyzyx.luckytool.hook.core

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

    /** 同形 YukiBaseHooker.appClassLoader：宿主应用类加载器（非空） */
    val appClassLoader: ClassLoader get() = Env.classLoader ?: error("classLoader not attached")

    /** 同形 YukiBaseHooker.appInfo：宿主包信息（App 分组必非空，legacy 调用点零改动） */
    val appInfo: ApplicationInfo get() = Env.appInfo ?: error("appInfo is null for non-app host")

    /** 同形 YukiBaseHooker.dataChannel：实时配置推送的远程偏好等价物 */
    val dataChannel: Channel get() = Channel

    /** 同形 prefs(ModulePrefs)：远程偏好读取（非空 getter 包装，对齐 legacy YukiHookPrefsBridge） */
    fun prefs(name: String): Env.NonNullPrefs = Env.prefs(name)

    /** 同形 loadHooker：装载子 Hooker，异常隔离 */
    fun loadHooker(hooker: Hooker) {
        runCatching { hooker.onHook() }
            .onFailure { Env.log(Log.ERROR, "HookRouter", "loadHooker ${hooker::class.java.name} failed", it) }
    }
}