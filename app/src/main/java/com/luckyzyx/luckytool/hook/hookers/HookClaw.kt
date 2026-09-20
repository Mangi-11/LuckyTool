package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.globals.HookGlobalSystemProperties
import com.luckyzyx.luckytool.hook.scopes.claw.RemoveRootDetection
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookClaw : Hooker {
    override fun onHook() {
        loadHooker(HookGlobalSystemProperties)

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            //移除Root检测
            if (prefs(ModulePrefs).getBoolean("remove_root_detection", false)) {
                loadHooker(RemoveRootDetection(dexKitBridge))
            }

        }

    }
}
