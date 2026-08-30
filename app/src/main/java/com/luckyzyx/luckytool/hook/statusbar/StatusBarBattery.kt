package com.luckyzyx.luckytool.hook.statusbar

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.scopes.systemui.StatusBarBatteryInfoNotify
import com.luckyzyx.luckytool.hook.scopes.systemui.StatusBarBatteryView
import com.luckyzyx.luckytool.utils.A12
import com.luckyzyx.luckytool.utils.SDK
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object StatusBarBattery : Hooker {
    override fun onHook() {
        //电池图标
        loadHooker(StatusBarBatteryView)

        //电池信息通知
        if (SDK >= A12) loadHooker(StatusBarBatteryInfoNotify)
    }
}