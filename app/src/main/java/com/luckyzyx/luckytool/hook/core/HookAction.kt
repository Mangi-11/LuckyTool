@file:Suppress("UNCHECKED_CAST")

package com.luckyzyx.luckytool.hook.core

import com.highcapable.kavaref.extension.ClassLoaderProvider
import com.highcapable.kavaref.resolver.ConstructorResolver
import com.highcapable.kavaref.resolver.MethodResolver
import com.highcapable.kavaref.resolver.base.MemberResolver
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Member
import java.lang.reflect.Method

/**
 * Hook 动作 DSL：形状对齐 YukiHookAPI 的 hook { } 块，
 * 内部统一落到 libxposed XposedInterface.hook().intercept。
 * 链语义同 legacy 惯例：
 * before → （未提前返回则 proceed 修改后的 args，异常入 throwable）→ after → 抛 throwable / 返回 result
 */
class HookAction internal constructor() {

    internal val beforeBlocks = mutableListOf<(HookCall) -> Unit>()
    internal val afterBlocks = mutableListOf<(HookCall) -> Unit>()
    internal var replaced = false
    internal var replacedValue: Any? = null
    internal var intercepted = false

    fun before(block: HookCall.() -> Unit) {
        beforeBlocks += block
    }

    fun after(block: HookCall.() -> Unit) {
        afterBlocks += block
    }

    /** 替换方法体：跳过原方法，直接返回给定值 */
    fun replaceTo(value: Any?) {
        replaced = true
        replacedValue = value
    }

    fun replaceToTrue() = replaceTo(true)

    fun replaceToFalse() = replaceTo(false)

    /** 完全阻断：原方法与 after 均不执行 */
    fun intercept() {
        intercepted = true
    }
}

/**
 * Hook 调用上下文：成员形状对齐 YukiHookAPI 的回调参数体
 */
class HookCall internal constructor(
    member: Member,
    instance: Any?,
    internal val arguments: Array<Any?>,
    hookClassLoader: ClassLoader?,
    hookPackageName: String?
) {

    private val memberRef: Member = member

    private val instanceRef: Any? = instance

    private val hookClassLoaderRef: ClassLoader? = hookClassLoader

    private val hookPackageNameRef: String? = hookPackageName

    /** 同形 legacy HookParam.packageName：hook 注册时刻的宿主包名快照 */
    val packageName: String
        get() = hookPackageNameRef ?: Env.packageName

    /**
     * 同形 legacy HookParam.appClassLoader：hook 注册时刻的分发 CL 快照优先，
     * 兜底 [Env.activeClassLoader]（进程 App CL / 当前分发包 CL）
     */
    val appClassLoader: ClassLoader
        get() = hookClassLoaderRef
            ?: Env.activeClassLoader()
            ?: error("appClassLoader unavailable")

    /** 同形 legacy HookParam.instance：非空（静态方法 hook 访问时抛错） */
    val instance: Any get() = instanceRef ?: error("instance is null for static hook")

    /** 同形 legacy HookParam.method：方法反射对象（构造器 hook 访问时抛错） */
    val method: Method get() = (memberRef as? Method) ?: error("constructor hook has no method")

    /** 同形 YukiHookAPI HookParam.args 属性：原始参数数组（可下标/集合操作，与 args() 函数并存） */
    val args: Array<Any?> get() = arguments

    /** 全部参数访问器 */
    fun args(): Args = Args(arguments, arguments.indices.toList())

    /** 指定下标参数访问器：args(0).cast<String>() */
    fun args(vararg indexes: Int): Args = Args(arguments, indexes.toList())

    /**
     * 结果值。赋值即视为提前返回（对齐 YukiHookAPI setResult 语义）：
     * before 阶段赋值后不再执行原方法
     */
    var result: Any? = null
        set(value) {
            field = value
            early = true
        }

    /** 异常。赋值即视为提前返回并抛出（对齐 YukiHookAPI setThrowable 语义） */
    var throwable: Throwable? = null
        set(value) {
            field = value
            early = true
        }

    fun hasThrowable(): Boolean = throwable != null

    /** 同形 YukiHookAPI 的 instanceOrNull 属性 */
    val instanceOrNull: Any? get() = instanceRef

    /** 死代码兼容：legacy if(false) 块中的 invokeOriginal，实际不会被调用 */
    fun invokeOriginal(vararg args: Any?): Any? =
        throw UnsupportedOperationException("invokeOriginal is not supported in native DSL")

    /** 提前返回 true（同形 YukiHookAPI 的 resultTrue） */
    fun resultTrue() {
        result = true
    }

    /** 提前返回 false（同形 YukiHookAPI 的 resultFalse） */
    fun resultFalse() {
        result = false
    }

    /** 提前返回 null（同形 YukiHookAPI 的 resultNull） */
    fun resultNull() {
        result = null
    }

    internal var early = false
}

/** 同形 YukiHookAPI 的 result<T>()：泛型取值（成员属性 result 提供裸引用） */
inline fun <reified T> HookCall.result(): T? = result as? T

/**
 * 参数访问器：first/last/下标 + 类型转换
 */
class Args internal constructor(
    private val array: Array<Any?>,
    private val indexes: List<Int>
) {

    val size: Int get() = indexes.size

    operator fun get(index: Int): Any? = array[indexes[index]]

    operator fun set(index: Int, value: Any?) {
        array[indexes[index]] = value
    }

    /** 同形 YukiHookAPI ArgsModifyer.set(value)：修改当前访问器指向的参数 */
    fun set(value: Any?) {
        array[indexes[0]] = value
    }

    /** 首参访问器（链式，对齐 YukiHookAPI：first().cast<T>() / first().any()） */
    fun first(): Args = Args(array, listOf(indexes.first()))

    /** 末参访问器（链式） */
    fun last(): Args = Args(array, listOf(indexes.last()))

    /** 裸值（同形 YukiHookAPI 的 any()） */
    fun any(): Any? = get(0)

    inline fun <reified T> cast(): T? = get(0) as? T

    fun string(): String = get(0) as? String ?: ""

    fun int(): Int = get(0) as? Int ?: 0

    fun long(): Long = get(0) as? Long ?: 0L

    fun boolean(): Boolean = get(0) as? Boolean ?: false

    /** 同形 YukiHookAPI ArgsModifyer.array<T>()：数组参数取值 */
    inline fun <reified T> array(): Array<T> =
        get(0) as? Array<T> ?: (arrayOfNulls<T>(0) as Array<T>)

    /** 同形 YukiHookAPI ArgsModifyer.list<T>()：List 参数取值 */
    fun <T> list(): List<T> = (get(0) as? List<T>) ?: emptyList()

    fun setTrue() {
        array[indexes[0]] = true
    }

    fun setFalse() {
        array[indexes[0]] = false
    }

    fun setNull() {
        array[indexes[0]] = null
    }
}

/**
 * Hook 入口：KavaRef 查找结果直接衔接 hook 动作块（方法/构造器通用，可空接收者）
 */
fun <M : Member> MemberResolver<M, *>.hook(priority: Int = 50, action: HookAction.() -> Unit) {
    when (val member = self) {
        is Method -> member.hookMethod(priority, action)
        is Constructor<*> -> member.hookMethod(priority, action)
        else -> Unit
    }
}

/** 同形 YukiHookAPI 的 hookAll：KavaRef method { } 返回的解析器列表全部挂动作块 */
fun <T : Any> List<MethodResolver<T>>.hookAll(priority: Int = 50, action: HookAction.() -> Unit) {
    forEach { it.hook(priority, action) }
}

@JvmName("hookAllOrNull")
fun <T : Any> List<MethodResolver<T>>?.hookAll(priority: Int = 50, action: HookAction.() -> Unit) {
    this?.hookAll(priority, action)
}

/** 单个解析器的 hookAll 别名（本项目用法等价 hook） */
fun <M : Member> MemberResolver<M, *>.hookAll(priority: Int = 50, action: HookAction.() -> Unit) =
    hook(priority, action)

/** 同形 YukiHookAPI 的 hookAll：KavaRef constructor { } 返回的构造器解析器列表全部挂动作块 */
@JvmName("hookAllCtors")
fun <T : Any> List<ConstructorResolver<T>>.hookAll(
    priority: Int = 50,
    action: HookAction.() -> Unit
) {
    forEach { it.hook(priority, action) }
}

@JvmName("hookAllOrNullCtors")
fun <T : Any> List<ConstructorResolver<T>>?.hookAll(
    priority: Int = 50,
    action: HookAction.() -> Unit
) {
    this?.hookAll(priority, action)
}

private fun Executable.hookMethod(priority: Int, action: HookAction.() -> Unit) {
    val base = Env.requireBase()
    val act = HookAction().apply(action)
    //hook 注册发生在宿主包 dispatch 的同步期：此刻 Env/ClassLoaderProvider 即该宿主包的
    //分发上下文，快照到 HookCall（等价 legacy HookParam 按包持有的解析语义）。
    val hookClassLoader = ClassLoaderProvider.classLoader
    val hookPackageName = Env.packageName.ifBlank { null }
    base.hook(this)
        .setPriority(priority)
        .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
        .intercept { chain ->
            //执行期把 KavaRef 全局 loader 钉在快照上：无 isFirstPackage 门禁后，
            //共享进程内后续包的 dispatch 会覆盖 Provider，异步回调里的无参
            //toClass()/toClassOrNull() 会漂移到别的包 CL（save/restore 保持嵌套栈语义）
            val prevProvider = ClassLoaderProvider.classLoader
            ClassLoaderProvider.classLoader = hookClassLoader
            try {
                val call = HookCall(
                    this,
                    chain.thisObject,
                    chain.args.toTypedArray(),
                    hookClassLoader,
                    hookPackageName
                )
                if (act.intercepted) return@intercept null
                act.beforeBlocks.forEach { it(call) }
                if (act.replaced && !call.early) call.result = act.replacedValue
                if (!call.early) {
                    try {
                        call.result = chain.proceed(call.arguments)
                    } catch (t: Throwable) {
                        call.throwable = t
                    }
                }
                act.afterBlocks.forEach { it(call) }
                call.throwable?.let { throw it }
                call.result
            } finally {
                ClassLoaderProvider.classLoader = prevProvider
            }
        }
}