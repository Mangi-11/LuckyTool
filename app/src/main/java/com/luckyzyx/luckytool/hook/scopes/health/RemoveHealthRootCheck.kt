package com.luckyzyx.luckytool.hook.scopes.health

import android.app.Activity
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveHealthRootCheck : Hooker {
    override fun onHook() {
        //Source SafetyCheckManager
        "com.heytap.health.safety.safetycheck.SafetyCheckManager".toClass().resolve().apply {
            firstMethod { parameters(Activity::class) }.hook {
                intercept()
            }
        }
    }
}