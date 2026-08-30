package com.luckyzyx.luckytool.hook.hookers

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.hook.scopes.multiapp.RemoveMultiAppBlacklist
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookMultiApp : YukiBaseHooker() {
    override fun onHook() {
        val osCode = getOSVersionCode


        //移除应用分身黑名单
        if (prefs(ModulePrefs).getBoolean("remove_multi_app_blacklist", false)) {
            if (osCode >= 31) loadHooker(RemoveMultiAppBlacklist)
        }
    }
}