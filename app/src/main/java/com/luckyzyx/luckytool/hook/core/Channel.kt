package com.luckyzyx.luckytool.hook.core

import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SettingsPrefs

/**
 * dataChannel 同形替代：legacy 的模块 App → 宿主进程实时推送通道，
 * 在 libxposed 下等价于远程偏好（ModulePrefs 优先，SettingsPrefs 兜底）：
 * - 注册时补读一次当前值（宽松替代 push 语义）
 * - 框架刷新远程偏好快照时若触发监听器则实现实时更新，不支持时降级为注册时一次
 *
 * 键不存在的场景（如 UI 从未写入）不回调，与 legacy wait 的"只在 push 时回调"一致。
 */
object Channel {

    inline fun <reified T> wait(key: String, noinline block: (T) -> Unit) {
        val prefs = Env.prefs(ModulePrefs).takeIf { it.contains(key) }
            ?: Env.prefs(SettingsPrefs)
        prefs.all[key]?.let { value -> (value as? T)?.let(block) }
        runCatching {
            prefs.registerOnSharedPreferenceChangeListener { changed, changedKey ->
                if (changedKey == key) (changed.all[key] as? T)?.let(block)
            }
        }
    }

    /**
     * legacy 无类型参数 wait 的等价物：块内忽略推送值、自行重读 prefs 的场景。
     * 注册时补触发一次 + 远程偏好快照变更时再次触发。
     */
    fun watch(key: String, block: () -> Unit) {
        val prefs = Env.prefs(ModulePrefs).takeIf { it.contains(key) }
            ?: Env.prefs(SettingsPrefs)
        block()
        runCatching {
            prefs.registerOnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == key) block()
            }
        }
    }
}
