package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveAodMusicWhitelist : Hooker {
    override fun onHook() {
        //Source AodMediaDataListener
        "com.oplusos.systemui.aod.mediapanel.AodMediaDataListener\$Companion".toClass().resolve().apply {
            firstMethod { name = "isAodMediaSupport" }.hook {
                replaceToTrue()
            }
            firstMethod { name = "isAodMediaSupportWithoutFeature" }.hook {
                replaceToTrue()
            }
        }
    }
}