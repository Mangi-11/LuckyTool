package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookMediaProjectionManager : Hooker {
    override fun onHook() {
        val isEnable =
            prefs(ModulePrefs).getBoolean("enable_record_calls_on_third_party_apps", false)

        //Source MediaProjectionManagerServiceExtImpl
        "android.media.projection.MediaProjectionManagerServiceExtImpl".toClassOrNull()?.resolve()?.apply {
            firstMethod { name = "isOplusApp";parameterCount = 1 }.hook {
                after {
                    if (!isEnable) return@after
                    val packageName = args().first().string()
                    if (packageName == "com.oplus.audiomonitor") resultTrue()
                }
            }
        }
    }
}