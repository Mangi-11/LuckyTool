package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.globals.HookGlobalFeatureConfig
import com.luckyzyx.luckytool.hook.globals.HookGlobalSystemProperties
import com.luckyzyx.luckytool.hook.scopes.phone.ForceDisplaySIMSomeSwitch
import com.luckyzyx.luckytool.utils.DexkitUtils
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookPhone : Hooker {
    override fun onHook() {
        loadHooker(HookGlobalFeatureConfig)
        loadHooker(HookGlobalSystemProperties)

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            //强制显示部分开关
            loadHooker(ForceDisplaySIMSomeSwitch(dexKitBridge))
        }
    }
}