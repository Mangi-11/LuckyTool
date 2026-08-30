package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ForceDisplayOfDeviceControlsTiles : Hooker {
    override fun onHook() {
        //Source OplusDeviceControlsTile
        "com.oplus.systemui.qs.tiles.OplusDeviceControlsTile".toClass().resolve().apply {
            firstMethod { name = "isAvailable" }.hook {
                replaceToTrue()
            }
        }
    }
}