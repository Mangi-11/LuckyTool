package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object AppSplashScreen : Hooker {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("disable_splash_screen", false)

        //Source StartingSurfaceController
        "com.android.server.wm.StartingSurfaceController".toClass().resolve().apply {
            firstMethod {
                name = "showStartingWindow"
                parameterCount = 5
            }.hook {
                if (isEnable) intercept()
            }
        }
    }
}