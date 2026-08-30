package com.luckyzyx.luckytool.hook.scopes.launcher

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveAppUpdateGreenDot : Hooker {
    override fun onHook() {
        val mode = prefs(ModulePrefs).getString("set_app_update_dot_display_mode", "0")
        if (mode != "2") return

        //Source BubbleTextView
        "com.android.launcher3.BubbleTextView".toClass().resolve().apply {
            firstMethod { name = "isShouldShowGreenDot" }.hook {
                replaceToFalse()
            }
        }
    }
}