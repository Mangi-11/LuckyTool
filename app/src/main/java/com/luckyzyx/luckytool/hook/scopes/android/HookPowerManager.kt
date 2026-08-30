package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookPowerManager : Hooker {
    override fun onHook() {
        val removeThermal =
            prefs(ModulePrefs).getBoolean("disable_temperature_control_listener", false)
        if (!removeThermal) return

        //Source PowerManager
        "android.os.PowerManager".toClass().resolve().apply {
            firstMethod {
                name = "addThermalStatusListener"
                parameterCount = 1
            }.hook {
                after {
                    val listener = args().first().any() ?: return@after
                    firstMethod {
                        name = "removeThermalStatusListener"
                        parameterCount = 1
                    }.of(instance).invoke(listener)
                }
            }
            firstMethod { name = "getCurrentThermalStatus" }.hook {
                replaceTo(0)
            }
        }
    }
}