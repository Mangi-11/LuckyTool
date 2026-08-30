package com.luckyzyx.luckytool.hook.scopes.soundrecorder

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookBaseUtil : Hooker {
    override fun onHook() {
        //Source BaseUtil
        "com.soundrecorder.base.utils.BaseUtil".toClass().resolve().apply {
            firstMethod { name = "isRealme" }.hook {
                replaceToTrue()
            }
        }
    }
}