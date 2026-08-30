package com.luckyzyx.luckytool.hook.scopes.launcher

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableLauncherIndicatorEntry : Hooker {
    override fun onHook() {
        //Source IndicatorEntry Companion
        "com.android.launcher3.search.IndicatorEntry\$Companion".toClass().resolve().apply {
            firstMethod { name = "isSupportIndicatorEntryMenu" }.hook {
                replaceToTrue()
            }
        }
    }
}