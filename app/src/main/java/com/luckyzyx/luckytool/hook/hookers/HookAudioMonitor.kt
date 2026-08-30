package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.audiomonitor.HookVoipRecorderService
import com.luckyzyx.luckytool.hook.scopes.audiomonitor.VoipRecorderWhitelist
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.getOSVersionCode

@Obfuscate
object HookAudioMonitor : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode

        //启用三方应用通话录音
        if (prefs(ModulePrefs).getBoolean("enable_record_calls_on_third_party_apps", false)) {
            if (osCode == 30) loadHooker(HookVoipRecorderService)
        }

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            //扩展第三方通话录音白名单
            if (prefs(ModulePrefs).getBoolean("expand_voip_recorder_whitelist", false)) {
                if (osCode >= 31) loadHooker(VoipRecorderWhitelist(dexKitBridge))
            }
        }
    }
}