package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object DisableMaliciousAppIntercept : Hooker {
    override fun onHook() {
        //Source OplusAppStartConfirmManager
        "com.android.server.wm.OplusAppStartConfirmManager".toClass().resolve().apply {
            firstMethod { name = "checkMaliciousIntercept" }.hook {
                intercept()
            }
        }
    }
}