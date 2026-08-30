package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.globals.HookGlobalFeatureConfig
import com.luckyzyx.luckytool.hook.scopes.mms.RemoveMmsBottomInputBoxMenu
import com.luckyzyx.luckytool.hook.scopes.mms.RemoveMmsCardMarketingButton
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookOplusMMS : Hooker {
    override fun onHook() {
        loadHooker(HookGlobalFeatureConfig)

        if (prefs(ModulePrefs).getBoolean("remove_mms_bottom_input_box_menu", false)) {
            loadHooker(RemoveMmsBottomInputBoxMenu)
        }

        if (prefs(ModulePrefs).getBoolean("remove_mms_card_marketing_button", false)) {
            loadHooker(RemoveMmsCardMarketingButton)
        }

    }
}