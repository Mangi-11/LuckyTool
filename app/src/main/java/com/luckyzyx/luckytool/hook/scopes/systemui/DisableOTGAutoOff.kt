package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object DisableSysUIOTGAutoOff : YukiBaseHooker() {
    override fun onHook() {
        // Search OtgHelper 600000
        VariousClass(
            "com.oplusos.systemui.notification.helper.OtgHelper", //C13
            "com.oplus.systemui.qs.helper.OtgHelper" //C14
        ).toClass().resolve().apply {
            firstMethod { name = "setAutoCloseAlarm" }.hook {
                intercept()
            }
        }
    }
}