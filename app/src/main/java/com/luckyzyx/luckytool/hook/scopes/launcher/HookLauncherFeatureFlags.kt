package com.luckyzyx.luckytool.hook.scopes.launcher

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookLauncherFeatureFlags : Hooker {
    override fun onHook() {
        val twoLine = prefs(ModulePrefs).getBoolean("enable_drawer_layout_double_line_names", false)

        //Source FeatureFlags
        "com.android.launcher3.config.FeatureFlags".toClass().resolve().apply {
            firstMethod {
                name = "getDebugFlag"
                parameterCount = 3
            }.hook {
                before {
                    val key = args().first().string()
//                    val defValue = args(1).boolean()
//                    val description = args().last().string()
                    when (key) {
                        "ENABLE_TWOLINE_ALLAPPS" -> if (twoLine) args(1).setTrue()
                    }
                }
            }
        }
    }
}