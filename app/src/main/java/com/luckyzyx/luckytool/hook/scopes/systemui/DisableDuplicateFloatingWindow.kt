package com.luckyzyx.luckytool.hook.scopes.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object DisableDuplicateFloatingWindow : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode

        //Source ClipboardOverlayController C13
        "com.android.systemui.clipboardoverlay.ClipboardOverlayController".toClass().resolve()
            .apply {
                firstMethodOrNull { name = "showSinglePreview" }?.hook {
                    after {
                        args().first().cast<View>()?.isVisible = false
                        firstField { name = "mView" }.of(instance).get<View>()?.isVisible = false
                    }
                }
            }

        if (osCode < 30) return

        //Source ClipboardOverlayView C14
        "com.android.systemui.clipboardoverlay.ClipboardOverlayView".toClass().resolve().apply {
            firstMethodOrNull { name = "showSinglePreview" }?.hook {
                after {
                    args().first().cast<View>()?.isVisible = false
                    instance<View>().isVisible = false
                }
            }
        }
    }
}