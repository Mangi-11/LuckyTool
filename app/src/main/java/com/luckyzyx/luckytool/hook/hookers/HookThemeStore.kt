package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.themestore.UnlockThemeStoreVip
import com.luckyzyx.luckytool.utils.ModulePrefs

@Obfuscate
object HookThemeStore : Hooker {
    override fun onHook() {
        //解锁主题商店VIP
        if (prefs(ModulePrefs).getBoolean("unlock_themestore_vip", false)) {
            loadHooker(UnlockThemeStoreVip)
        }
    }
}