package com.luckyzyx.luckytool.hook.scopes.systemui

import android.view.View
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.safeOfNull
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ControlCenterBackgroundTransParency : Hooker {
    override fun onHook() {
        var customAlpha =
            prefs(ModulePrefs).getInt("custom_control_center_background_transparency", -1)
        dataChannel.wait<Int>("custom_control_center_background_transparency") {
            customAlpha = it
        }

        //Source ScrimController
        "com.android.systemui.statusbar.phone.ScrimController".toClass().resolve().apply {
            firstMethod { name = "updateScrimColor" }.hook {
                before {
                    if (customAlpha < 0) return@before
                    val value = customAlpha / 10.0F
                    val view = args().first().cast<View>() ?: return@before
                    val alpha = args(1).cast<Float>() ?: return@before
                    val name = safeOfNull { view.resources.getResourceEntryName(view.id) }
                        ?: return@before
                    when (name) {
                        "scrim_in_front" -> {}
                        "scrim_behind" -> if (alpha > value) args(1).set(value)
                        "scrim_notifications" -> if (alpha > value) args(1).set(value)
                    }
                }
            }
        }
    }
}