package com.luckyzyx.luckytool.hook.scopes.android

import android.view.KeyEvent
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ReducePowerMenuDisplayDelay : Hooker {
    override fun onHook() {
        var isEnable = prefs(ModulePrefs).getBoolean("reduce_power_menu_display_delay", false)
        dataChannel.wait<Boolean>("reduce_power_menu_display_delay") { isEnable = it }

        //Source PhoneWindowManager -> PowerKeyRule -> super getVeryLongPressTimeoutMs
        "com.android.server.policy.SingleKeyGestureDetectorExtImpl".toClass().resolve().apply {
            firstMethod { name = "modifyPressTimeout" }.hook {
                after {
                    if (!isEnable) return@after
                    val pressType = args().first().cast<Int>() ?: return@after
                    val event = args().last().cast<KeyEvent>() ?: return@after
                    if (pressType == 1 && event.keyCode == 26) result = 800L
                }
            }
        }
    }
}