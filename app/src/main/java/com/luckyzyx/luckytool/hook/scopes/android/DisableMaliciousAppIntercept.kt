package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object DisableMaliciousAppIntercept : YukiBaseHooker() {
    override fun onHook() {
        //Source OplusAppStartConfirmManager
        "com.android.server.wm.OplusAppStartConfirmManager".toClass().resolve().apply {
            firstMethod { name = "checkMaliciousIntercept" }.hook {
                intercept()
            }
        }
    }
}