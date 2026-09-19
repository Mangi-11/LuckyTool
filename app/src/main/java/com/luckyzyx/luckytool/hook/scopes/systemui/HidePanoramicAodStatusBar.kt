package com.luckyzyx.luckytool.hook.scopes.systemui

import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HidePanoramicAodStatusBar : YukiBaseHooker() {
    override fun onHook() {
        val statusBar = "com.oplus.systemui.statusbar.phone.KeyguardStatusBarViewExImpl"
            .toClassOrNull() ?: return
        val aodData = "com.oplus.systemui.aod.aodclock.constant.AodData"
            .toClassOrNull() ?: return
        val getInstance = aodData.getDeclaredMethod("getInstance", Context::class.java)
        val isPanoramicAod = aodData.getDeclaredMethod("isPanoramicAod")
        val context = statusBar.getDeclaredField("context").apply { isAccessible = true }

        statusBar.resolve().apply {
            firstMethod {
                name = "hookDozingState"
                parameters(Boolean::class)
            }.hook {
                before {
                    if (args(0).boolean()) {
                        val data = getInstance.invoke(null, context.get(instance))
                        if (isPanoramicAod.invoke(data) == true) resultFalse()
                    }
                }
            }
        }
    }
}
