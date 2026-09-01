package com.luckyzyx.luckytool.hook.scopes.settings

import android.content.Context
import android.os.customize.OplusCustomizeRestrictionManager
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object DisableSettingOtgAutoOff : Hooker {
    override fun onHook() {
        //Source OtgConnectionOpenedPreferenceController
        VariousClass(
            "com.oplus.settings.feature.othersettings.controller.OtgConnectionOpenedPreferenceController",  //C14
            "com.oplus.settings.feature.spfunction.OtgConnectionOpenedPreferenceController"  //C14.1 C15
        ).toClass().resolve().apply {
            firstMethod { name = "isPreferenceSupport" }.hook {
                before {
                    val context = args().first().cast<Context>() ?: return@before
                    val isUSBOtgDisabled =
                        OplusCustomizeRestrictionManager.getInstance(context).isUSBOtgDisabled
                    result = !isUSBOtgDisabled
                }
            }
        }
    }
}