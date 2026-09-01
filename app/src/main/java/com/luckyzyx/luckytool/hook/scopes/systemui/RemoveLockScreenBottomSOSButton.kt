package com.luckyzyx.luckytool.hook.scopes.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveLockScreenBottomSOSButton : Hooker {
    override fun onHook() {
        //Source OplusEmergencyButtonControllExImpl
        VariousClass(
            "com.oplus.systemui.keyguard.OplusEmergencyButtonControllExImpl", //C13
            "com.oplus.keyguard.OplusEmergencyButtonExImpl" //C14 C15
        ).toClass().resolve().apply {
            firstMethodOrNull { name = "disableShowEmergencyButton" }?.hook {
                replaceToTrue()
            } ?: firstMethodOrNull { name = "shouldUpdateEmergencyCallButton" }?.hook {
                before {
                    firstField { name = "mEmergencyButton" }.of(instance).get<View>()
                        ?.isVisible = false
                    resultTrue()
                }
            } ?: firstMethod { name = "updateEmergencyCallButton" }.hook {
                before {
                    args().last().setFalse()
                }
            }
        }
    }
}