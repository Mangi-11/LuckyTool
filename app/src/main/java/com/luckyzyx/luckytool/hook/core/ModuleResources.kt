package com.luckyzyx.luckytool.hook.core

import android.content.res.AssetManager
import android.content.res.Resources

/**
 * 模块 APK 资源（宿主进程内）。
 * 宿主 PackageManager 查不到被框架隐藏的模块包，因此不能用
 * getResourcesForApplication(BuildConfig.APPLICATION_ID)；
 * 这里从 moduleApplicationInfo.sourceDir 直接构造 AssetManager 加载。
 */
object ModuleResources {

    fun create(host: Resources): Resources? {
        val appInfo = Env.moduleAppInfo ?: return null
        return runCatching {
            val assets = AssetManager::class.java.newInstance()
            AssetManager::class.java
                .getDeclaredMethod("addAssetPath", String::class.java)
                .apply { isAccessible = true }
                .invoke(assets, appInfo.sourceDir)
            Resources(assets, host.displayMetrics, host.configuration)
        }.getOrNull()
    }
}