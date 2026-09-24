package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object DisableVolumeBarThicknessEffect : YukiBaseHooker() {
    override fun onHook() {
        //Source OplusVolumeDialogImpl C14+
        "com.oplus.systemui.volume.OplusVolumeDialogImpl".toClass().resolve().apply {
            firstMethod { name = "startThickAnim" }.hook {
                intercept()
            }
        }
    }
}