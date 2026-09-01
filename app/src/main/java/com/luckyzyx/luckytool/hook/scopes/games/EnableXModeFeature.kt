package com.luckyzyx.luckytool.hook.scopes.games

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableXModeFeature : Hooker {
    override fun onHook() {
        //Source CoolingBackClipHelper / CoolingBackClipFeature
        VariousClass(
            "business.module.perfmode.CoolingBackClipHelper", //V8
            "business.module.perfmode.CoolingBackClipFeature" //V9.0.0
        ).toClass().resolve().apply {
            firstMethod { parameterCount = 1;returnType = Any::class }.hook {
                after {
                    resultTrue()
                }
            }
        }
    }
}