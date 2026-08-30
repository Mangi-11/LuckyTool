package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveStatusBarTopNotification : Hooker {
    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("remove_statusbar_top_notification", false)

        //Source AlertWindowNotification
        "com.android.server.wm.AlertWindowNotification".toClass().resolve().apply {
            firstMethod { name = "onPostNotification" }.hook {
                if (isEnable) intercept()
            }
        }
    }
}