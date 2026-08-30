package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.cloudservice.DisableForcedBackupAppList
import com.luckyzyx.luckytool.hook.scopes.cloudservice.RemoveNetworkRestriction
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs

@Obfuscate
object HookCloudService : Hooker {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            //移除网络限制
            if (prefs(ModulePrefs).getBoolean("remove_network_limit", false)) {
                loadHooker(RemoveNetworkRestriction(dexKitBridge))
            }
        }

        //启用自定义备份选项
        if (prefs(ModulePrefs).getBoolean("disable_forced_backup_app_list", false)) {
            loadHooker(DisableForcedBackupAppList)
        }
    }
}