package com.luckyzyx.luckytool.hook.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClassOrNull

/**
 * 同形 YukiHookAPI 的 onAppLifecycle：宿主 Application 可用后执行块。
 * Application 尚未创建时（冷启动早期分发）挂 Application#onCreate 等待，每进程只触发一次。
 */
fun Hooker.onAppLifecycle(block: AppLifecycleScope.() -> Unit) {
    Env.hostContext()?.let {
        runCatching { AppLifecycleScope(it).block() }
        return
    }
    "android.app.Application".toClassOrNull()?.resolve()?.apply {
        firstMethodOrNull { name = "onCreate" }?.hook {
            after {
                runCatching { AppLifecycleScope(instance as Context).block() }
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
        runCatching { context.registerReceiver(receiver, IntentFilter(action)) }
    }
}
