package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveSystemPromptIcon : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode
        loadHooker(SystemPromptIconV13)
    }

    @Obfuscate
    object SystemPromptIconV13 : Hooker {
        override fun onHook() {
            //Source SystemPromptController
            VariousClass(
                "com.oplusos.systemui.statusbar.policy.SystemPromptController", //C13
                "com.oplus.systemui.statusbar.controller.SystemPromptController" //C14
            ).toClass().resolve().apply {
                firstMethod { name = "updatePromptIcon" }.hook {
                    intercept()
                }
            }
        }
    }
}