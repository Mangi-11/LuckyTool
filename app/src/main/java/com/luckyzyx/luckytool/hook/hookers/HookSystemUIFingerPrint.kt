package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.systemui.FingerPrintIconAnim

@Obfuscate
object HookSystemUIFingerPrint : Hooker {
    override fun onHook() {
        //指纹图标
        loadHooker(FingerPrintIconAnim)
    }
}