package com.luckyzyx.luckytool.hook.core

import android.content.pm.ApplicationInfo
import android.util.Log

/**
 * Hooker 基接口：对应迁移前 YukiBaseHooker 的常用成员面，
 * 使既有 hook 文件只需替换基类与 import。
 */
interface Hooker {

    fun onHook()

    /** 同形 YukiBaseHooker.packageName：该 Hooker 分发时的宿主包名（wrapper 语义，不随共享进程漂移） */
    val packageName: String get() = HookRouter.contextOf(this)?.packageName ?: Env.packageName

    val processName: String get() = Env.processName

    /** 同形 YukiBaseHooker.classLoader：该 Hooker 分发上下文的 appClassLoader */
    val classLoader: ClassLoader?
        get() = HookRouter.contextOf(this)?.appClassLoader ?: Env.activeClassLoader()

    /** 同形 YukiBaseHooker.appClassLoader：非空版（legacy 中即非空） */
    val appClassLoader: ClassLoader
        get() = HookRouter.contextOf(this)?.appClassLoader
            ?: Env.activeClassLoader()
            ?: error("classLoader not attached")

    /** 同形 YukiBaseHooker.appInfo：该 Hooker 分发时的宿主包信息（App 分组必非空） */
    val appInfo: ApplicationInfo
        get() = HookRouter.contextOf(this)?.appInfo
            ?: Env.appInfo
            ?: error("appInfo is null for non-app host")

    /** 同形 YukiBaseHooker.dataChannel：实时配置推送的远程偏好等价物 */
    val dataChannel: Channel get() = Channel

    /** 同形 prefs(ModulePrefs)：远程偏好读取（非空 getter 包装，对齐 legacy YukiHookPrefsBridge） */
    fun prefs(name: String): Env.NonNullPrefs = Env.prefs(name)

    /** 同形 loadHooker：装载子 Hooker（继承父 Hooker 的分发上下文，异常隔离） */
    fun loadHooker(hooker: Hooker) {
        runCatching {
            HookRouter.assignContext(hooker, HookRouter.contextOf(this))
            hooker.onHook()
        }.onFailure {
            Env.log(Log.ERROR, "HookRouter", "loadHooker ${hooker::class.java.name} failed", it)
        }
    }
}