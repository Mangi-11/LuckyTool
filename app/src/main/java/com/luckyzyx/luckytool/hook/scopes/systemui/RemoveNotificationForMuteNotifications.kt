package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveNotificationForMuteNotifications : Hooker {
    override fun onHook() {
        //Source NoDisturbController
        VariousClass(
            "com.oplusos.systemui.statusbar.controller.NoDisturbController",
            "com.oplus.systemui.statusbar.controller.NoDisturbController" //C14
        ).toClass().resolve().apply {
            firstMethod { name = "checkBlockBannerStatus" }.hook {
                replaceToFalse()
            }
        }
    }
}