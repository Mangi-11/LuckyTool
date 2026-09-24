package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveDoNotDisturbModeNotification : YukiBaseHooker() {
    override fun onHook() {
        //Source DndAlertHelper
        VariousClass(
            "com.oplusos.systemui.notification.helper.DndAlertHelper",
            "com.coloros.systemui.notification.helper.DndAlertHelper",
            "com.oplus.systemui.statusbar.notification.helper.DndAlertHelper" //C14
        ).toClass().resolve().apply {
            firstMethod { name = "operateNotification" }.hook {
                intercept()
            }
        }
    }
}