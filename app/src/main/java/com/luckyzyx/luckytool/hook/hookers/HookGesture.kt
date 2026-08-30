package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.globals.HookGlobalFeatureConfig
import com.luckyzyx.luckytool.hook.scopes.gesture.CustomAonGestureScrollPageWhitelist
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookGesture : Hooker {
    override fun onHook() {
        loadHooker(HookGlobalFeatureConfig)

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            //自定义滑动页面白名单
            //自定义视频手势白名单
            if (prefs(ModulePrefs).getBoolean("force_enable_aon_gestures", false)) {
                loadHooker(CustomAonGestureScrollPageWhitelist(dexKitBridge))
            }
        }
    }
}