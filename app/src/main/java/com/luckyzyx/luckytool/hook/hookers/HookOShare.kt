package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.scopes.oshare.RemoveOShareCloseCountDown
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookOShare : Hooker {
    override fun onHook() {

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            //移除互传关闭倒计时
            if (prefs(ModulePrefs).getBoolean("remove_oshare_close_countdown", false)) {
                loadHooker(RemoveOShareCloseCountDown(dexKitBridge))
            }

        }

    }
}