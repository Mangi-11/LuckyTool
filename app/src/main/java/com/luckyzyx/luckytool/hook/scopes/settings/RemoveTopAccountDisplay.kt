package com.luckyzyx.luckytool.hook.scopes.settings

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.SDK
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveTopAccountDisplay : Hooker {
    override fun onHook() {
        //Source UserPreferenceController
        "com.oplus.settings.feature.homepage.user.UserPreferenceController".toClass().resolve()
            .apply {
                firstMethod {
                    name = if (SDK >= A13) "checkAvailable"
                    else "getAvailabilityStatus"
                }.hook {
                    if (SDK >= A13) replaceToFalse() else replaceTo(3)
                }
            }
    }
}