package com.luckyzyx.luckytool.hook.scopes.systemui

import android.content.Context
import android.view.View
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.kavaref.extension.classOf
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveRotateScreenButton : Hooker {
    override fun onHook() {
        //Source FloatingRotationButton
        VariousClass(
            "com.android.systemui.statusbar.phone.FloatingRotationButton", //A11
            "com.android.systemui.navigationbar.gestural.FloatingRotationButton", //A12
            "com.android.systemui.shared.rotation.FloatingRotationButton" //C13 C14
        ).toClass().resolve().apply {
            firstConstructor { parameters { it[0] == classOf<Context>() } }.hook {
                after {
                    firstField { name = "mKeyButtonView" }.of(instance).get<View>()
                        ?.isVisible = false
                }
            }
        }
    }
}