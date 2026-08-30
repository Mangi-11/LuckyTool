package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveFlashlightOpenNotification : Hooker {
    override fun onHook() {
        //Source FlashlightNotification
        VariousClass(
            "com.oplusos.systemui.flashlight.FlashlightNotification", //C13
            "com.oplus.systemui.statusbar.notification.flashlight.FlashlightNotification", //C14
            "com.oplus.systemui.notification.flashlight.FlashlightNotification" //C15.0.1
        ).toClass().resolve().apply {
            firstMethod { name = "sendNotification";parameterCount = 1 }.hook {
                intercept()
            }
        }
    }
}