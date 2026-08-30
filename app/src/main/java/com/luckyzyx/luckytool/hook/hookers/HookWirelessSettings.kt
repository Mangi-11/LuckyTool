package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.scopes.wirelesssettings.EnableWifiDetailsDisplayGateway
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookWirelessSettings : Hooker {
    override fun onHook() {

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            //启用WiFi详情显示网关
            if (prefs(ModulePrefs).getBoolean("enable_wifi_details_display_gateway", false)) {
                loadHooker(EnableWifiDetailsDisplayGateway(dexKitBridge))
            }

        }
    }
}