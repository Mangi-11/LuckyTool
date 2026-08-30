package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ForceEnable32BitSupport : Hooker {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("force_enable_32_bit_support", false)
        if (!isEnable) return

        //Source OplusPackageManagerHelper
        "com.android.server.pm.OplusPackageManagerHelper".toClass().resolve().apply {
            firstMethod { name = "allowInstall32BitApp" }.hook {
                replaceToTrue()
            }
        }
    }
}