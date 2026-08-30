package com.luckyzyx.luckytool.hook.scopes.ota

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class HookNotificationHelper(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        val notifySuccess =
            prefs(ModulePrefs).getBoolean("remove_ota_notify_install_success", false)

        //Source NotificationHelper
        dexKitBridge.findClass {
            matcher {
//                addFieldForType(Context::class.java)
//                addFieldForType(Notification.Builder::class.java)
//                addFieldForType(NotificationManager::class.java)
                usingStrings(
                    "NotificationHelper",
                    "ota_notify_new_channel_id",
                    "ota_notify_new_channel_default_id"
                )
            }
        }.apply {
            checkDataList("NotificationHelper")

            if (notifySuccess) findMethod {
                matcher {
                    paramCount(0)
                    usingStrings("notifyInstallSuccess")
                }
            }.apply {
                checkDataList("notifyInstallSuccess")

                single().className.toClass().resolve().apply {
                    firstMethod {
                        name = single().methodName
                        emptyParameters()
                    }.hook {
                        intercept()
                    }
                }
            }
        }
    }
}