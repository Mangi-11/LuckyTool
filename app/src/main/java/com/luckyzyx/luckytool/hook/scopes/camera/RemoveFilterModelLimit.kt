package com.luckyzyx.luckytool.hook.scopes.camera

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveFilterModelLimit : Hooker {
    override fun onHook() {
        //Source SystemUtil
        "com.oplus.ocs.camera.ipusdk.processunit.filter.list.SystemUtil".toClass().resolve().apply {
            firstMethod { name = "isMarketNameContainSeriesNum" }.hook {
                replaceToTrue()
            }
        }
    }
}