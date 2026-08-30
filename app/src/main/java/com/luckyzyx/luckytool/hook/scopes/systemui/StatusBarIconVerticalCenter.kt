package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object StatusBarIconVerticalCenter : Hooker {
    override fun onHook() {
        //Source PhoneStatusBarViewExImpl updateContentsPadding
        VariousClass(
            "com.oplusos.systemui.ext.BasePhoneStatusBarViewExt",
            "com.oplus.systemui.statusbar.phone.PhoneStatusBarViewExImpl"
        ).toClass().resolve().apply {
            firstMethod { name = "getHoleTop" }.hook {
                replaceTo(0)
            }
            firstMethod { name = "getHoleBottom" }.hook {
                replaceTo(0)
            }
        }
    }
}