package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.XLog
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveAlwaysAllowAppStartList : Hooker {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("enable_always_allow_app_start_dialog", false)
        if (!isEnable) return

        var controller: Any? = null

        dataChannel.wait<ArrayList<Int>>("remove_always_allow_app_start_list") {
            it.forEachIndexed { _, i ->
                controller?.asResolver()?.firstMethod {
                    name = "onUserRemoved"
                    parameters(Int::class)
                }?.invoke(i)
            }
            XLog.debug("cleaning $it always start app list")
        }

        //Source OplusSecurityPermissionManager
        "com.android.server.am.OplusSecurityPermissionManager".toClass().resolve().apply {
            firstMethod { name = "init" }.hook {
                after {
                    controller =
                        firstField { type = "com.android.server.am.OplusActivityStartController" }
                            .of(instance).get()
                }
            }
        }
    }
}