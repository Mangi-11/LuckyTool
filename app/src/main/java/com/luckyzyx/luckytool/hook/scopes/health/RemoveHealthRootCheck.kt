package com.luckyzyx.luckytool.hook.scopes.health

import android.app.Activity
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveHealthRootCheck : YukiBaseHooker() {
    override fun onHook() {
        //Source SafetyCheckManager
        "com.heytap.health.safety.safetycheck.SafetyCheckManager".toClass().resolve().apply {
            firstMethod { parameters(Activity::class) }.hook {
                intercept()
            }
        }
    }
}