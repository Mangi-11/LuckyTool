package com.luckyzyx.luckytool.hook.core

import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.ClassLoaderProvider
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.highcapable.kavaref.resolver.FieldResolver

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
 * 模块资源包 id 段（--package-id 0x64），模块资源 id 均落在此段，宿主资源不冲突。
 * 未落段的 id 直接走宿主实现（super，宿主 assets 已传入），避免每资源 try/catch。
 */
private const val MODULE_PKG_ID = 0x64

/**
 * 自动注入模块资源（对齐 legacy YukiHookAPI 加载 App 时的模块资源可用语义）：
 * hook ResourcesManager.getResources 出口，把每次创建的宿主 Resources 包装为
 * 可解析模块资源段（0x64）的 [DualResources]。所有 Context/Activity 从此出口取资源，
 * 一处包装全局生效。每进程注册一次。
 */
object ModuleResourcesHook {

    @Volatile
    private var installed = false

    private val wrapped = java.util.WeakHashMap<Resources, Resources>()

    fun install() {
        if (installed) return
        installed = true
        "android.content.res.ResourcesManager".toClassOrNull()?.resolve()?.apply {
            method { name = "getResources" }.hookAll {
                after {
                    val host = result as? Resources ?: return@after
                    if (host is DualResources) return@after
                    result = wrap(host)
                }
            }
        }
    }

    private fun wrap(host: Resources): Resources = wrapped[host] ?: run {
        ModuleResources.create(host)?.let { module ->
            DualResources(host, module).also { wrapped[host] = it }
        } ?: host
    }
}

/**
 * 注入模块资源（同形 YukiHookAPI injectModuleAppResources）：
 * 把当前 Context 的 Resources 反射替换为 [DualResources]——
 * 模块资源段（0x64）优先，宿主资源兜底
 */
fun Context.injectModuleAppResources() {
    runCatching {
        val hostRes = resources
        val moduleRes = ModuleResources.create(hostRes) ?: return@runCatching
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
 * 双查找资源包装：模块 id（0x64 段）走模块资源、查不到回落宿主；
 * 宿主 id 直接走 super（构造时传入宿主 assets，天然可解析）。
 */
class DualResources internal constructor(
    private val host: Resources,
    private val module: Resources
) : Resources(host.assets, host.displayMetrics, host.configuration) {

    /** 模块段查询：不落段直接宿主，落段时模块优先、异常回落宿主 */
    private inline fun <T> withModule(id: Int, hostGetter: () -> T, moduleGetter: (Int) -> T): T {
        if (id ushr 24 != MODULE_PKG_ID) return hostGetter()
        return try {
            moduleGetter(id)
        } catch (_: Resources.NotFoundException) {
            hostGetter()
        }
    }

    override fun getAnimation(id: Int): XmlResourceParser =
        withModule(id, { super.getAnimation(id) }, { module.getAnimation(it) })

    override fun getBoolean(id: Int): Boolean =
        withModule(id, { super.getBoolean(id) }, { module.getBoolean(it) })

    override fun getColor(id: Int): Int =
        withModule(id, { super.getColor(id) }, { module.getColor(it) })

    override fun getColor(id: Int, theme: Resources.Theme?): Int =
        withModule(id, { super.getColor(id, theme) }, { module.getColor(it, theme) })

    override fun getDimension(id: Int): Float =
        withModule(id, { super.getDimension(id) }, { module.getDimension(it) })

    override fun getDimensionPixelOffset(id: Int): Int =
        withModule(id, { super.getDimensionPixelOffset(id) }, { module.getDimensionPixelOffset(it) })

    override fun getDimensionPixelSize(id: Int): Int =
        withModule(id, { super.getDimensionPixelSize(id) }, { module.getDimensionPixelSize(it) })

    override fun getDrawable(id: Int): Drawable? =
        withModule(id, { super.getDrawable(id) }, { module.getDrawable(it) })

    override fun getDrawable(id: Int, theme: Resources.Theme?): Drawable? =
        withModule(id, { super.getDrawable(id, theme) }, { module.getDrawable(it, theme) })

    override fun getDrawableForDensity(id: Int, density: Int): Drawable? =
        withModule(id, { super.getDrawableForDensity(id, density) }, { module.getDrawableForDensity(it, density) })

    override fun getDrawableForDensity(id: Int, density: Int, theme: Resources.Theme?): Drawable? =
        withModule(id, { super.getDrawableForDensity(id, density, theme) }, { module.getDrawableForDensity(it, density, theme) })

    override fun getIdentifier(name: String?, defType: String?, defPackage: String?): Int =
        module.getIdentifier(name, defType, defPackage).takeIf { it != 0 }
            ?: host.getIdentifier(name, defType, defPackage)

    override fun getIntArray(id: Int): IntArray =
        withModule(id, { super.getIntArray(id) }, { module.getIntArray(it) })

    override fun getInteger(id: Int): Int =
        withModule(id, { super.getInteger(id) }, { module.getInteger(it) })

    override fun getLayout(id: Int): XmlResourceParser =
        withModule(id, { super.getLayout(id) }, { module.getLayout(it) })

    override fun getQuantityString(id: Int, quantity: Int): String =
        withModule(id, { super.getQuantityString(id, quantity) }, { module.getQuantityString(it, quantity) })

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any?): String =
        withModule(id, { super.getQuantityString(id, quantity, *formatArgs) }, { module.getQuantityString(it, quantity, *formatArgs) })

    override fun getQuantityText(id: Int, quantity: Int): CharSequence =
        withModule(id, { super.getQuantityText(id, quantity) }, { module.getQuantityText(it, quantity) })

    override fun getString(id: Int): String =
        withModule(id, { super.getString(id) }, { module.getString(it) })

    override fun getString(id: Int, vararg formatArgs: Any?): String =
        withModule(id, { super.getString(id, *formatArgs) }, { module.getString(it, *formatArgs) })

    override fun getStringArray(id: Int): Array<String> =
        withModule(id, { super.getStringArray(id) }, { module.getStringArray(it) })

    override fun getText(id: Int): CharSequence =
        withModule(id, { super.getText(id) }, { module.getText(it) })

    override fun getText(id: Int, def: CharSequence?): CharSequence =
        withModule(id, { super.getText(id, def) }, { module.getText(it, def) })

    override fun getTextArray(id: Int): Array<CharSequence> =
        withModule(id, { super.getTextArray(id) }, { module.getTextArray(it) })

    //注：Resources.getValue(int, TypedValue, boolean) 与 openRawResource 为
    //镜像隐藏 API（android.jar 不可见），无法 override；这些路径的模块资源由
    // getIdentifier/其他可覆盖方法兜底

    override fun getXml(id: Int): XmlResourceParser =
        withModule(id, { super.getXml(id) }, { module.getXml(it) })
}