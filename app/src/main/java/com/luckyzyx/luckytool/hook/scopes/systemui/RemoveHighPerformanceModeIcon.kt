package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveHighPerformanceModeIcon : Hooker {
    override fun onHook() {
        //Source PhoneStatusBarPolicyEx
        VariousClass(
            "com.oplusos.systemui.statusbar.phone.PhoneStatusBarPolicyEx",
            "com.oplus.systemui.statusbar.phone.OplusPhoneStatusBarPolicyExImpl" //C14
        ).toClass().resolve().apply {
            firstMethod {
                name = "updateHighPerformanceIcon"
                emptyParameters()
            }.hook {
                before {
                    firstField { name = "highPerformanceMode" }.of(instance).set(false)
                }
            }
        }
    }
}