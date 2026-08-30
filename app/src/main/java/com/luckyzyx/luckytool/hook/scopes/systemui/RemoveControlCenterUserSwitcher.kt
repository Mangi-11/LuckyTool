package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveControlCenterUserSwitcher : Hooker {
    override fun onHook() {
        //Search Log showUserSwitcher
        "com.oplusos.systemui.qs.OplusQSFooterImpl".toClass().resolve().apply {
            firstMethod {
                name = "showUserSwitcher"
                emptyParameters()
                returnType = Boolean::class
            }.hook {
                replaceToFalse()
            }
        }
    }
}