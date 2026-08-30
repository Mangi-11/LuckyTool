package com.luckyzyx.luckytool.hook.scopes.settings

import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableSwipeUpNavigationGesture : Hooker {
    override fun onHook() {
        //Source NavBarSettingsValueUtil
        "com.oplus.settings.feature.navbar.NavBarSettingsValueUtil".toClass().resolve().apply {
            firstMethod {
                name = "getGestureUpModeAvailable"
                parameters(Context::class)
                returnType = Int::class
            }.hook {
                replaceTo(0)
            }
        }
    }
}