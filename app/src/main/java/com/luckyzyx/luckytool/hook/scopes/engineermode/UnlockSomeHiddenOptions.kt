package com.luckyzyx.luckytool.hook.scopes.engineermode

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object UnlockSomeHiddenOptions : Hooker {
    override fun onHook() {
        //Source SecrecyServiceHelper
        "com.oplus.engineermode.impl.SecrecyServiceHelper".toClass().resolve().apply {
            firstMethod { name = "isSecrecySupported" }.hook {
                replaceToTrue()
            }
            firstMethod { name = "getSecrecyState" }.hook {
                replaceToFalse()
            }
        }
    }
}