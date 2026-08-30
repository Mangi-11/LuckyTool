package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.scopes.health.RemoveHealthRootCheck
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookHealth : Hooker {
    override fun onHook() {

        //移除Root检测对话框
        if (prefs(ModulePrefs).getBoolean("remove_health_root_check_dialog", false)) {
            loadHooker(RemoveHealthRootCheck)
        }

    }
}