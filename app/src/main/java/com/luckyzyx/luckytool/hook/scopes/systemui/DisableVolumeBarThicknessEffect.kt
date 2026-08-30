package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object DisableVolumeBarThicknessEffect : Hooker {
    override fun onHook() {
        //Source OplusVolumeDialogImpl C14+
        "com.oplus.systemui.volume.OplusVolumeDialogImpl".toClass().resolve().apply {
            firstMethod { name = "startThickAnim" }.hook {
                intercept()
            }
        }
    }
}