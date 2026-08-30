package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.mediacontroller.ForceEnableMediaMusicFluidCloudRipple
import com.luckyzyx.luckytool.utils.getOSVersionCode

@Obfuscate
object HookMediaController : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode

        //强制启用媒体音乐流体云波纹
        if (osCode >= 33) loadHooker(ForceEnableMediaMusicFluidCloudRipple)
    }
}