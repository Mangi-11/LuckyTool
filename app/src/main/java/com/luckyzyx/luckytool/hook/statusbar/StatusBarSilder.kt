package com.luckyzyx.luckytool.hook.statusbar

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.systemui.ControllerCenterSliderTransparency
import com.luckyzyx.luckytool.utils.getOSVersionCode

@Obfuscate
object StatusBarSilder : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode

        //控制中心滑动条透明度
        if (osCode in 26..33) loadHooker(ControllerCenterSliderTransparency)

    }
}