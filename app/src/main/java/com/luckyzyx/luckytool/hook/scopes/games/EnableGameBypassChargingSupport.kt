package com.luckyzyx.luckytool.hook.scopes.games

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableGameBypassChargingSupport : Hooker {
    override fun onHook() {
        //Source COSAExportedImpl
        "com.oplus.cosa.exported.COSAExportedImpl".toClass().resolve().apply {
            firstMethod { name = "getBypassChargingDeviceSupport" }.hook{
                replaceTo(2)
            }
        }
    }
}