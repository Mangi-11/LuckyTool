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
        val getInstance = aodData.resolve().optional().firstMethodOrNull {
            name = "getInstance"
            parameters(Context::class)
        } ?: return
        val isPanoramicAod = aodData.resolve().optional().firstMethodOrNull {
            name = "isPanoramicAod"
            parameters()
            returnType = Boolean::class
        } ?: return
        val context = statusBar.resolve().optional().firstFieldOrNull {
            name = "context"
            type = Context::class
        } ?: return
        val hookDozingState = statusBar.resolve().optional().firstMethodOrNull {
            name = "hookDozingState"
            parameters(Boolean::class)
            returnType = Boolean::class
        } ?: return

        hookDozingState.hook {
            before {
                if (args(0).boolean()) {
                    val ctx = context.copy().of(instance).get<Context>() ?: return@before
                    val data = getInstance.invoke<Any>(ctx) ?: return@before
                    if (isPanoramicAod.copy().of(data).invoke<Boolean>() == true) resultFalse()
                }
            }
        }
    }
}
