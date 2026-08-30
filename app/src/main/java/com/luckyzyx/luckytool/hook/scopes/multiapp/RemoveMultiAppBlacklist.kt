package com.luckyzyx.luckytool.hook.scopes.multiapp

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveMultiAppBlacklist : Hooker {
    override fun onHook() {
        //Source MultiAppBlackListUpdateHelper
        "com.oplus.multiapp.utils.MultiAppBlackListUpdateHelper".toClass().resolve().apply {
            firstMethod { name = "loadMultiappBlackListConfig" }.hook {
                intercept()
            }
        }
    }
}