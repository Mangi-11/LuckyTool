package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.scopes.externalstorage.RemoveStorageLimit
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookExternalStorage : Hooker {
    override fun onHook() {
        //移除存储限制
        if (prefs(ModulePrefs).getBoolean("remove_storage_limit", false)) {
            loadHooker(RemoveStorageLimit)
        }
    }
}