package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveLockScreenCloseNotificationButton : Hooker {
    override fun onHook() {
        //Source NotificationPanelViewExt
        VariousClass(
            "com.oplusos.systemui.notification.extend.NotificationPanelViewExt",  //C12 C13
            "com.oplus.systemui.notification.extend.OplusNotificationCloseButtonImp"  //C14
        ).toClass().resolve().apply {
            firstMethod { name = "setNotificationCloseButton" }.hook {
                intercept()
            }
        }
    }
}