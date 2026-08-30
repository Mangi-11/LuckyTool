package com.luckyzyx.luckytool.hook.core

import android.util.Log

/**
 * 日志出口：宿主进程内落到 libxposed 框架日志（XposedInterface.log），
 * 迁移波次中逐步替换 YLog。
 */
object XLog {

    const val TAG = "LuckyTool"

    fun v(msg: String, tag: String = TAG) = Env.log(Log.VERBOSE, tag, msg)

    fun d(msg: String, tag: String = TAG, t: Throwable? = null) = Env.log(Log.DEBUG, tag, msg, t)

    fun i(msg: String, tag: String = TAG) = Env.log(Log.INFO, tag, msg)

    fun w(msg: String, tag: String = TAG, t: Throwable? = null) = Env.log(Log.WARN, tag, msg, t)

    fun e(msg: String, tag: String = TAG, t: Throwable? = null) = Env.log(Log.ERROR, tag, msg, t)

    //YLog 同形别名（迁移文件批量替换后兼容原调用名）
    fun verbose(msg: String, tag: String = TAG, t: Throwable? = null) = v(msg, tag)

    fun debug(msg: String, tag: String = TAG, t: Throwable? = null) = d(msg, tag, t)

    fun info(msg: String, tag: String = TAG, t: Throwable? = null) = i(msg, tag)

    fun warn(msg: String, tag: String = TAG, t: Throwable? = null) = w(msg, tag, t)

    fun error(msg: String, tag: String = TAG, t: Throwable? = null) = e(msg, tag, t)
}