package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.keyguardclock.KeyGuardcLockRedMode
import com.luckyzyx.luckytool.utils.DexkitUtils

@Obfuscate
object HookKeyguardClock : Hooker {
    override fun onHook() {

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            //锁屏时钟
            loadHooker(KeyGuardcLockRedMode(dexKitBridge))
        }

    }
}
