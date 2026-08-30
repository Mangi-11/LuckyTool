package com.luckyzyx.luckytool.hook.scopes.systemui

import android.view.View
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HideLockScreenStatusBarDisplay : Hooker {
    override fun onHook() {
        //Source KeyguardStatusBarView
        VariousClass(
            "com.android.systemui.statusbar.phone.KeyguardStatusBarView",  //C12.1 C13.1
            "com.android.systemui.statusbar.phone.KeyguardStatusBarView"  //C14
        ).toClass().resolve().apply {
            firstMethod { name = "setVisibility" }.hook {
                before {
                    args().first().set(View.INVISIBLE)
                }
            }
        }
    }
}