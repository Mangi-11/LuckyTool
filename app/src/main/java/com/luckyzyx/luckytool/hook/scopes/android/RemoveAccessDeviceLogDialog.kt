package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveAccessDeviceLogDialog : Hooker {

    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("remove_access_device_log_dialog", false)

        //Source LogcatManagerService
        "com.android.server.logcat.LogcatManagerService".toClass().resolve().apply {
            firstMethod { name = "processNewLogAccessRequest" }.hook {
                before {
                    if (!isEnable) return@before
                    val client = args().first().any() ?: return@before
                    firstMethod { name = "onAccessApprovedForClient" }.of(instance).invoke(client)
                    resultNull()
                }
            }
        }
    }
}