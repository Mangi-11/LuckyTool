package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import com.luckyzyx.luckytool.utils.A14
import com.luckyzyx.luckytool.utils.SDK
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveGTModeNotification : Hooker {
    override fun onHook() {
        //Source GTUtils
        VariousClass(
            "com.oplusos.systemui.statusbar.util.GTUtils", //C13
            "com.oplus.systemui.statusbar.util.GTUtils" //C14
        ).toClass().resolve().apply {
            firstMethod {
                name = if (SDK >= A14) "notifyOpenGtMode"
                else "showOpenGtModeNotify"
            }.hook {
                intercept()
            }
        }
    }
}