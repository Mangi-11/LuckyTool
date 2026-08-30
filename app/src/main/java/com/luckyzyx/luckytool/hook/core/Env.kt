package com.luckyzyx.luckytool.hook.core

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.util.Log
import com.highcapable.kavaref.extension.ClassLoaderProvider
import com.luckyzyx.luckytool.hook.core.Env.enter
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

    /** 宿主进程 App Context（ActivityThread.currentApplication），替换 onAppLifecycle 类用法 */
    fun hostContext(): Context? = runCatching {
        Class.forName("android.app.ActivityThread")
            .getDeclaredMethod("currentApplication").apply { isAccessible = true }
            .invoke(null) as? Context
    }.getOrNull()

    /** 远程偏好：读模块 App 的同名 SharedPreferences（框架按 group 快照下发） */
    fun prefs(name: String): NonNullPrefs {
        //框架不支持远程偏好时（PROP_CAP_REMOTE 缺失）所有开关将静默失效，主动告警一次
        if (!remotePrefsWarned && base != null &&
            frameworkProperties and XposedInterface.PROP_CAP_REMOTE == 0L
        ) {
            remotePrefsWarned = true
            log(
                Log.WARN,
                "LuckyTool",
                "framework does not support remote preferences, all prefs fall back to defaults"
            )
        }
        val raw = runCatching { requireBase().getRemotePreferences(name) }.getOrElse { EmptyPrefs }
        return NonNullPrefs(raw)
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
        override fun getStringSet(
            key: String, defValues: MutableSet<String>?
        ): MutableSet<String>? = defValues

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

    /**
     * 非空偏好包装（对齐 legacy YukiHookPrefsBridge 形状）：
     * - getString/getStringSet 返回非空（Android SharedPreferences 为 @Nullable 标注）
     * - 全部 getter 带 legacy 默认值（getString ""、getBoolean false、数值 0、Set 空集），
     *   单参调用点（legacy YukiHookPrefsBridge 有默认值参数）零改动
     * - 监听器与 edit 原样委托
     */
    class NonNullPrefs internal constructor(private val inner: SharedPreferences) {

        val all: MutableMap<String, *> get() = inner.all

        fun getString(key: String, defValue: String? = ""): String =
            inner.getString(key, defValue) ?: (defValue ?: "")

        fun getStringSet(key: String, defValues: MutableSet<String>? = null): MutableSet<String> =
            inner.getStringSet(key, defValues)?.toMutableSet() ?: (defValues ?: mutableSetOf())

        fun getInt(key: String, defValue: Int = 0): Int = inner.getInt(key, defValue)

        fun getLong(key: String, defValue: Long = 0L): Long = inner.getLong(key, defValue)

        fun getFloat(key: String, defValue: Float = 0f): Float = inner.getFloat(key, defValue)

        fun getBoolean(key: String, defValue: Boolean = false): Boolean =
            inner.getBoolean(key, defValue)

        fun contains(key: String): Boolean = inner.contains(key)

        fun edit(): SharedPreferences.Editor = inner.edit()

        fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?
        ) = inner.registerOnSharedPreferenceChangeListener(listener)

        fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?
        ) = inner.unregisterOnSharedPreferenceChangeListener(listener)
    }
}