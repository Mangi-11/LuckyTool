package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveDanmakuNotificationWhitelist : Hooker {
    override fun onHook() {
        //Source DanmakuHelper
        VariousClass(
            "com.oplusos.systemui.notification.helper.DanmakuHelper", //C13
//            "com.oplus.systemui.statusbar.notification.helper.HeadsUpHelper" //C14
        ).toClass().resolve().apply {
            firstMethod {
                name = "isSupportDanmaku"
            }.hook {
                replaceToTrue()
            }
        }
    }
}