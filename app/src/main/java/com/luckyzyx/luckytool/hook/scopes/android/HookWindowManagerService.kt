package com.luckyzyx.luckytool.hook.scopes.android

import android.content.Context
import android.provider.Settings
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.utils.A14
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookWindowManagerService : Hooker {
    override fun onHook() {
        //移除DPI重启恢复
        var isDpi = prefs(ModulePrefs).getBoolean("remove_dpi_restart_recovery", false)
        dataChannel.wait<Boolean>("remove_dpi_restart_recovery") { isDpi = it }

        val windowManagerService = "com.android.server.wm.WindowManagerService"

        //Source DisplayWindowSettings
        //分辨率切换时 density==初始密度会传 0 清空持久化值，替换为用户强制密度
        if (SDK >= A14) {
            "com.android.server.wm.DisplayWindowSettings".toClass().resolve().apply {
                method {
                    name = "setForcedDensity"
                    parameterCount { it in 2..3 }
                }.hookAll {
                    before {
                        if (!isDpi) return@before
                        val density = args(1).int()
//                    val userId = if (method.parameterCount == 3) args().last().int() else null
//                    YLog.debug("${method.name} is call -> $density | $userId")

                        val service = firstField { type = windowManagerService }.of(instance).get()
                            ?: return@before
                        val context = service.asResolver().firstField { type = Context::class }
                            .get<Context>() ?: return@before
                        val resolver = context.contentResolver
                        val forcedDensity = Settings.Secure.getString(
                            resolver, "display_density_forced"
                        )?.toIntOrNull() ?: return@before
                        if (density == 0) args(1).set(forcedDensity)
                    }
                }
            }
        }

        //Source DisplayContentExtImpl
        if (SDK >= A14) {
            "com.android.server.wm.DisplayContentExtImpl".toClass().resolve().apply {
                firstMethod {
                    name = "setForcedDisplayInfoForWmSize"
                    parameterCount = 5
                }.hook {
                    before {
                        if (!isDpi) return@before
//                    val width = args().first().int()
//                    val height = args(1).int()
//                    val density = args(2).int()
//                    val userId = args(3).int()
                        val service = args().last().any() ?: return@before
//                    YLog.debug("${method.name} is call -> $width | $height | $density | $userId")

                        val context = service.asResolver().firstField { type = Context::class }
                            .get<Context>() ?: return@before
                        val resolver = context.contentResolver
                        val forcedDensity = Settings.Secure.getString(
                            resolver, "display_density_forced"
                        )?.toIntOrNull() ?: return@before
                        args(2).set(forcedDensity)
                    }
                }
            }
        }

        //Source OplusResolutionSwitchImpl
        "com.android.server.wm.OplusResolutionSwitchImpl".toClass().resolve().apply {
            firstMethodOrNull { name = "resetDensityIfNeed" }?.hook {
                before {
                    if (isDpi) resultNull()
                }
            } ?: run {
                firstMethod { name = "onResolutionSettingsChange"; parameterCount = 1 }.hook {
                    before {
                        if (isDpi) args().first().setFalse()
                    }
                }
                firstMethodOrNull {
                    name = "onFakeResolutionSettingsChange"
                    parameterCount = 1
                }?.hook {
                    before {
                        if (isDpi) args().first().setFalse()
                    }
                }
            }
        }
    }
}