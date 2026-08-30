package com.luckyzyx.luckytool.hook.scopes.settings

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object AllowDisablingSystemApps : Hooker {
    override fun onHook() {
        //Source AppButtonsPreferenceControllerAdaptor
        "com.oplus.settings.adaptor.AppButtonsPreferenceControllerAdaptor".toClass().resolve().apply {
            firstMethod { name = "setUninstallButtonEnabled" }.hook {
                before {
                    args().first().set(true)
                }
            }
        }
    }
}