package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object OplusWindowSecureFlag : Hooker {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("remove_screenshot_privacy_limit", false)
        if (!isEnable) return

        //Source OplusLongshotMainWindow
        "com.android.server.wm.OplusLongshotMainWindow".toClass().resolve().apply {
            firstMethod { name = "hasSecure" }.hook {
                replaceToFalse()
            }
        }
    }
}