package com.luckyzyx.luckytool.hook.scopes.uiengine

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveAodNotificationWhitelist : Hooker {
    override fun onHook() {
        //Source NotificationView -> BaseView
        "com.oplus.egview.widget.BaseView".toClass().resolve().apply {
            firstMethod { name = "isExpRegion" }.hook {
                replaceToTrue()
            }
        }
    }
}