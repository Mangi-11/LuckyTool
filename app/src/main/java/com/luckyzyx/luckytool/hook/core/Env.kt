package com.luckyzyx.luckytool.hook.core

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.util.Log
import com.highcapable.kavaref.extension.ClassLoaderProvider
import io.github.libxposed.api.XposedInterface

/**
 * libxposed 运行环境持有器：LibXposedEntry 注入框架接口，
 * 每进入一个包回调时通过 [enter] 更新当前宿主上下文。
 */
object Env {

    var base: XposedInterface? = null
        private set

    var packageName: String = ""
        private set

    var processName: String = ""
        private set

    var classLoader: ClassLoader? = null
        private set

    var appInfo: ApplicationInfo? = null
        private set

    /** 框架能力位（XposedInterface.PROP_CAP_*），onModuleLoaded 时注入 */
    var frameworkProperties: Long = 0L
        private set

    /** 远程偏好能力告警只打一次 */
    private var remotePrefsWarned = false

    /** 模块装载（每个进程一次） */
    fun attach(base: XposedInterface, processName: String, frameworkProperties: Long = 0L) {
        this.base = base
        this.processName = processName
        this.frameworkProperties = frameworkProperties
    }

    /** 进入宿主包回调：切换 KavaRef 反射 ClassLoader 与上下文 */
    fun enter(packageName: String, classLoader: ClassLoader, appInfo: ApplicationInfo?) {
        this.packageName = packageName
        this.classLoader = classLoader
        this.appInfo = appInfo
        ClassLoaderProvider.classLoader = classLoader
    }

    fun requireBase(): XposedInterface =
        base ?: error("libxposed not attached, make sure LibXposedEntry is loaded")

    /** 远程偏好：读模块 App 的同名 SharedPreferences（框架按 group 快照下发） */
    fun prefs(name: String): SharedPreferences {
        //框架不支持远程偏好时（PROP_CAP_REMOTE 缺失）所有开关将静默失效，主动告警一次
        if (!remotePrefsWarned && base != null &&
            frameworkProperties and XposedInterface.PROP_CAP_REMOTE == 0L
        ) {
            remotePrefsWarned = true
            log(Log.WARN, "LuckyTool", "framework does not support remote preferences, all prefs fall back to defaults")
        }
        return runCatching { requireBase().getRemotePreferences(name) }.getOrElse { EmptyPrefs }
    }

    /** 输出到框架日志，框架不可用时回落 logcat */
    fun log(priority: Int, tag: String, msg: String, t: Throwable? = null) {
        runCatching { requireBase().log(priority, tag, msg, t) }
            .onFailure { Log.println(priority, tag, msg) }
    }

    /** 框架远程偏好不可用时的兜底：所有 getter 返回默认值，绝不抛异常 */
    private object EmptyPrefs : SharedPreferences {
        override fun getAll(): MutableMap<String, *> = mutableMapOf<String, Any?>()
        override fun getString(key: String, defValue: String?): String? = defValue
        override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? = defValues
        override fun getInt(key: String, defValue: Int): Int = defValue
        override fun getLong(key: String, defValue: Long): Long = defValue
        override fun getFloat(key: String, defValue: Float): Float = defValue
        override fun getBoolean(key: String, defValue: Boolean): Boolean = defValue
        override fun contains(key: String): Boolean = false
        override fun edit(): SharedPreferences.Editor = throw UnsupportedOperationException()
        override fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?
        ) = Unit

        override fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?
        ) = Unit
    }
}