package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object BatteryOptimizationWhitelist : Hooker {
    override fun onHook() {
        val isEnable =
            prefs(ModulePrefs).getBoolean("restore_default_battery_optimization_whitelist", false)
        val disableCustom = false
//            prefs(ModulePrefs).getBoolean("disable_customize_battery_optimization_whiteList", false)
        if (!isEnable) return

        //Source oplus-service-jobscheduler -> OplusDeviceIdleHelper
        //Search sys_deviceidle_whitelist
        "com.android.server.OplusDeviceIdleHelper".toClass().resolve().apply {
            (firstMethodOrNull { name = "getNewWhiteList" }
                ?: firstMethod { name = "getNewWhiteListLocked" }).hook {
                before {
                    val whiteListAll = args().first().cast<java.util.ArrayList<String>>()
                    whiteListAll?.clear()
                    val mDefaultWhitelist = firstField { name = "mDefaultWhitelist" }
                        .get<List<String>>() ?: listOf()
                    whiteListAll?.addAll(mDefaultWhitelist)

                    if (!disableCustom) firstMethod { name = "getCustomizeWhiteList" }.of(instance)
                        .invoke(whiteListAll)
                    firstMethod { name = "addNfcJapanFelica" }.of(instance).invoke(whiteListAll)
        //                    whiteListAll?.add("com.oplus.upgradeguide")
                    resultNull()
                }
            }
        }
    }
}