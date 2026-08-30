package com.luckyzyx.luckytool.hook.scopes.systemui

import android.graphics.drawable.Drawable
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object CustomFluidCloudIconBackgroundTransparency : Hooker {
    override fun onHook() {
        var customAlpha =
            prefs(ModulePrefs).getInt("custom_fluid_cloud_icon_background_transparency", -1)
        dataChannel.wait<Int>("custom_fluid_cloud_icon_background_transparency") {
            customAlpha = it
        }

        //Source CapsuleViewBg
        "com.oplus.systemui.plugins.seedling.capsule.CapsuleViewBg".toClass().resolve().apply {
            firstMethod { name = "onDraw" }.hook {
                before {
                    if (customAlpha < 0) return@before
                    firstField { name = "customDrawable" }.of(instance).get<Drawable>()?.apply {
                        alpha = 255 / 10 * customAlpha
                    }
                }
            }
        }
    }
}