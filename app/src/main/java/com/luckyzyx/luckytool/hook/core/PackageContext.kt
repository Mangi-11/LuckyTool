package com.luckyzyx.luckytool.hook.core

import android.content.pm.ApplicationInfo

/**
 * 分发上下文（对齐 legacy YukiHookAPI 的 PackageParam.wrapper 语义）：
 * HookRouter 在每个宿主包分发时创建并绑定给对应 hooker，每 hooker 持有一份，
 * 后续包的 dispatch 不受影响——host 进程内的全局 Env 值不再是上下文唯一来源。
 *
 * appClassLoader 解析链与 legacy PackageParam 一致：
 * 手动设置值（等价 currentClassLoader setter）→ 分发时宿主包 CL（等价 wrapper.appClassLoader）
 * → 宿主进程 Application CL（等价 AppParasitics.currentApplication）。
 */
class PackageContext internal constructor(
    val packageName: String,
    val classLoader: ClassLoader,
    val appInfo: ApplicationInfo?
) {

    /** 手动覆盖的 CL（等价 legacy currentClassLoader 的 setter 语义） */
    var currentClassLoader: ClassLoader? = null

    val appClassLoader: ClassLoader
        get() = currentClassLoader
            ?: classLoader
            ?: Env.hostContext()?.classLoader
            ?: error("appClassLoader unavailable")
}