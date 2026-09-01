package com.luckyzyx.luckytool.hook.core

import android.app.Application
import android.app.Instrumentation
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.highcapable.kavaref.KavaRef.Companion.resolve

/**
 * 同形 YukiHookAPI 的 onAppLifecycle：宿主 Application 可用后执行块。
 * Application 尚未创建时（冷启动早期分发），对齐 YukiHookAPI 的做法——
 * hook Instrumentation.callApplicationOnCreate 的 after 再执行块
 * （宿主 Application 类会 override onCreate，hook 基类 Application#onCreate 不会触发）。
 */
fun Hooker.onAppLifecycle(block: AppLifecycleScope.() -> Unit) {
    Env.hostContext()?.let {
        runCatching { AppLifecycleScope(it).block() }
        return
    }
    Instrumentation::class.resolve().apply {
        firstMethodOrNull {
            name = "callApplicationOnCreate"
            parameters(Application::class)
        }?.hook {
            after {
                args().first().cast<Application>()?.let { app ->
                    runCatching { AppLifecycleScope(app).block() }
                }
            }
        }
    }
}

/** onAppLifecycle 作用域：宿主 Context + 同形 onCreate / registerReceiver */
class AppLifecycleScope(private val context: Context) {

    private val receivers = mutableListOf<BroadcastReceiver>()

    /** 同形 YukiHookAPI onCreate：此时 context 即宿主 Application */
    fun onCreate(block: Context.() -> Unit) {
        runCatching { block(context) }
    }

    /** 同形 YukiHookAPI registerReceiver：注册广播接收器，引用保持在 scope 内防 GC */
    fun registerReceiver(action: String, block: (Context, Intent) -> Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                runCatching { block(ctx, intent) }
            }
        }
        receivers += receiver
        runCatching {
            //Android 14+ 动态注册需显式 exported 标志（对齐 YukiHookAPI exported = true），
            //否则非豁免广播（如 OPPO ADDITIONAL_BATTERY_CHANGED）注册会静默抛 SecurityException
            if (Build.VERSION.SDK_INT >= 33) {
                context.registerReceiver(receiver, IntentFilter(action), Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(receiver, IntentFilter(action))
            }
        }
    }
}