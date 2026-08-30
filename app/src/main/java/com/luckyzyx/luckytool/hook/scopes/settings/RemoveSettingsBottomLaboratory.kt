package com.luckyzyx.luckytool.hook.scopes.settings

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveSettingsBottomLaboratory : Hooker {
    override fun onHook() {
        //Source TopLevelLaboratoryPreferenceController
        "com.oplus.settings.feature.homepage.TopLevelLaboratoryPreferenceController".toClass()
            .resolve().apply {
                firstMethod { name = "getAvailabilityStatus" }.hook {
                    replaceTo(3)
                }
            }
    }
}