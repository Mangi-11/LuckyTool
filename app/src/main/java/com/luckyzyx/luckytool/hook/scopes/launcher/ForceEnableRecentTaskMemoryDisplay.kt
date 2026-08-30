package com.luckyzyx.luckytool.hook.scopes.launcher

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ForceEnableRecentTaskMemoryDisplay : Hooker {
    override fun onHook() {
        //Source MemoryInfoManager
        "com.oplus.quickstep.memory.MemoryInfoManager".toClass().resolve().apply {
            firstMethod { name = "isAllowMemoryInfoDisplay" }.hook {
                replaceToTrue()
            }
            firstMethod { name = "needMemoryDetail" }.hook {
                replaceToTrue()
            }
        }
    }
}