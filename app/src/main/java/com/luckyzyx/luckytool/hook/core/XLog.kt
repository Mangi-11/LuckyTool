package com.luckyzyx.luckytool.hook.core

import android.util.Log

/**
 * 日志出口：宿主进程内落到 libxposed 框架日志（XposedInterface.log），
 * 迁移波次中逐步替换 YLog。
 */
object XLog {

    const val TAG = "LuckyTool"

    fun v(msg: String, tag: String = TAG) = Env.log(Log.VERBOSE, tag, msg)

    fun d(msg: String, tag: String = TAG) = Env.log(Log.DEBUG, tag, msg)

    fun i(msg: String, tag: String = TAG) = Env.log(Log.INFO, tag, msg)

    fun w(msg: String, tag: String = TAG) = Env.log(Log.WARN, tag, msg)

    fun e(msg: String, tag: String = TAG, t: Throwable? = null) = Env.log(Log.ERROR, tag, msg, t)
}