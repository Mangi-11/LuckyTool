package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.beaconlink.RemoveBeaconLinkTimeLimit
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.getOSVersionCode

@Obfuscate
object HookBeaconLink : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            //移除无网畅聊时间限制
            if (prefs(ModulePrefs).getBoolean("remove_beacon_link_time_limit", false)) {
             if (osCode >= 33)   loadHooker(RemoveBeaconLinkTimeLimit(dexKitBridge))
            }

        }

    }
}