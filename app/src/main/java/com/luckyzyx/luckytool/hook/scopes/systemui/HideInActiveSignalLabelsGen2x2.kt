package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HideInActiveSignalLabelsGen2x2 : YukiBaseHooker() {
    override fun onHook() {
        //Source MobileIconSets -> Companion -> config_isSystemUiExpSignalUi
        VariousClass(
            "com.oplus.systemui.statusbar.policy.MobileIconSets", //C13 C15
            "com.oplusos.systemui.statusbar.policy.MobileIconSets" //C14
        ).toClass(initialize = true).resolve().apply {
            val volteIcon = firstFieldOrNull { name = "VOLTE_ICON" } ?: return
            val volteIconEx = firstFieldOrNull { name = "VOLTE_ICON_EX" } ?: return
            volteIcon.set(volteIconEx.get<IntArray>() ?: return)
        }
    }
}