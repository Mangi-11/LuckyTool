package com.luckyzyx.luckytool.hook.core

import android.content.pm.ApplicationInfo

/**
 * 分发上下文（对齐 legacy YukiHookAPI 的 PackageParamWrapper / assignInstance 语义）：
 * 每个宿主包回调产生一个上下文，由 HookRouter 绑定给该包注册的每个 Hooker；
 * hooker 后续无论何时读取 packageName/classLoader 都拿到自己被分发时的宿主包信息，
 * 共享进程里其它包的后续 dispatch 不影响已分发 hooker。
 *
 * appClassLoader 解析链与 legacy PackageParam 一致：
 * 手动设置值（currentClassLoader）→ 分发时宿主包 CL（wrapper.appClassLoader）
 * → 宿主进程 Application CL（AppParasitics.currentApplication）。
 */
class PackageContext internal constructor(
    val packageName: String,
    val classLoader: ClassLoader?,
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