@file:Suppress("UNCHECKED_CAST")

package com.luckyzyx.luckytool.hook.core

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
    val method: Member,
    val instance: Any?,
    internal val arguments: Array<Any?>
) {

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
}

/**
 * Hook 入口：KavaRef 查找结果直接衔接 hook 动作块（方法/构造器通用，可空接收者）
 */
fun <M : Member> MemberResolver<M, *>?.hook(priority: Int = 50, action: HookAction.() -> Unit) {
    this ?: return
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
fun <M : Member> MemberResolver<M, *>?.hookAll(priority: Int = 50, action: HookAction.() -> Unit) =
    hook(priority, action)

private fun Executable.hookMethod(priority: Int, action: HookAction.() -> Unit) {
    val base = Env.requireBase()
    val act = HookAction().apply(action)
    base.hook(this)
        .setPriority(priority)
        .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
        .intercept { chain ->
            val call = HookCall(this, chain.thisObject, chain.args.toTypedArray())
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
        }
}