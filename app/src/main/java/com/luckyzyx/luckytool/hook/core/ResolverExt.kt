package com.luckyzyx.luckytool.hook.core

import android.content.Context
import android.content.res.Resources
import com.highcapable.kavaref.extension.ClassLoaderProvider
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.kavaref.resolver.FieldResolver
import com.luckyzyx.luckytool.BuildConfig

/**
 * KavaRef 字段解析器的泛型取值（同形 YukiHookAPI 的 get<T>()）。
 * 调用点带类型参数时优先本扩展，裸 get() 仍走 KavaRef 成员。
 */
fun <R> FieldResolver<*>.get(): R? = (get() as? R)

/**
 * 同形 YukiHookAPI 的 instance<T>()：非空强转（legacy 语义，as T）。
 * HookCall 的成员属性 [HookCall.instance] 提供裸可空引用，instanceOrNull 提供可空取值。
 */
inline fun <reified T> HookCall.instance(): T = instance as T

/**
 * 同形 legacy YukiBaseHooker 成员扩展的 VariousClass.toClass()：委托 KavaRef load()。
 * legacy 里 VariousClass 没有 toClass 扩展，由 PackageParam 成员扩展提供。
 * 返回 Class<Any>（非 *），保持 resolve() 结果不变性，.of(instance) 调用点才能匹配。
 */
fun VariousClass.toClass(
    loader: ClassLoader? = ClassLoaderProvider.classLoader, initialize: Boolean = false
): Class<Any> = load(loader, initialize)

/** 同形 legacy 的 VariousClass.toClassOrNull()：委托 KavaRef loadOrNull() */
fun VariousClass.toClassOrNull(
    loader: ClassLoader? = ClassLoaderProvider.classLoader, initialize: Boolean = false
): Class<Any>? = loadOrNull(loader, initialize)

/**
 * 注入模块资源（同形 YukiHookAPI injectModuleAppResources）：
 * 把当前 Context 的 Resources 反射替换为双查找包装器——
 * 模块资源 id（package-id 0x64）优先，宿主资源 id 兜底，
 * 使 R.string/R.id 与宿主 getIdentifier 同时可用
 */
fun Context.injectModuleAppResources() {
    runCatching {
        val hostRes = resources
        val moduleRes = packageManager.getResourcesForApplication(BuildConfig.APPLICATION_ID)
        val dual = DualResources(hostRes, moduleRes)
        var cls: Class<*>? = javaClass
        while (cls != null) {
            runCatching {
                cls.getDeclaredField("mResources").apply {
                    isAccessible = true
                    set(this@injectModuleAppResources, dual)
                }
            }.onSuccess { return }
            cls = cls.superclass
        }
    }.onFailure { XLog.w("injectModuleAppResources failed", t = it) }
}

/**
 * 双查找资源包装：模块 id 优先（模块资源包 id 为保留段 0x64，不会与宿主 0x7f 冲突），
 * 查不到时回落到宿主资源
 */
private class DualResources(private val host: Resources, private val module: Resources) :
    Resources(host.assets, host.displayMetrics, host.configuration) {

    override fun getString(id: Int): String =
        runCatching { module.getString(id) }.getOrElse { host.getString(id) }

    override fun getString(id: Int, vararg formatArgs: Any?): String =
        runCatching { module.getString(id, *formatArgs) }.getOrElse { host.getString(id, *formatArgs) }

    override fun getText(id: Int): CharSequence =
        runCatching { module.getText(id) }.getOrElse { host.getText(id) }

    override fun getText(id: Int, def: CharSequence?): CharSequence =
        runCatching { module.getText(id, def) }.getOrElse { host.getText(id, def) }

    override fun getIdentifier(name: String?, defType: String?, defPackage: String?): Int =
        module.getIdentifier(name, defType, defPackage).takeIf { it != 0 }
            ?: host.getIdentifier(name, defType, defPackage)
}