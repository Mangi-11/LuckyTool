package com.luckyzyx.luckytool.hook.scopes.packageinstaller

import android.content.Context
import android.preference.PreferenceManager
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hookMethod
import org.luckypray.dexkit.DexKitBridge

/** Keep the modern installer after repeated user cancellations, without overriding other checks. */
class KeepAppDetailInstaller(private val bridge: DexKitBridge) : Hooker {
    @Suppress("DEPRECATION")
    override fun onHook() {
        bridge.findMethod {
            matcher {
                paramTypes(Context::class.java, String::class.java)
                returnType(Int::class.java)
                usingStrings("count_canceled_by_app_detail", "com.oplus.appdetail")
            }
        }.single().getMethodInstance(appClassLoader).hookMethod {
            before {
                val context = args(0).cast<Context>() ?: return@before
                val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                val key = "count_canceled_by_app_detail"
                // The system persists this counter across reboots and falls back at five.
                // Reset only the reached threshold; the original method still checks availability,
                // disabled state and enterprise policy before selecting AppDetail.
                if (preferences.getInt(key, 0) >= 5) {
                    preferences.edit().putInt(key, 0).apply()
                }
            }
        }
    }
}
