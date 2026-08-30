package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableKeepNotificationWhenAppStop : Hooker {
    override fun onHook() {
        var isEnable =
            prefs(ModulePrefs).getBoolean("enable_keep_notification_when_app_stop", false)
        dataChannel.wait<Boolean>("enable_keep_notification_when_app_stop") { isEnable = it }

        //Source NotificationManagerService -> cancelAllNotificationsInt
        //Source OplusNotificationManagerServiceExtImpl -> shouldKeepNotifcationWhenForceStop
        //Source OplusNotificationCommonPolicy -> shouldKeepNotifcationWhenForceStop
        "com.android.server.notification.OplusNotificationManagerServiceExtImpl".toClass().resolve()
            .apply {
                firstMethod { name = "shouldKeepNotifcationWhenForceStop" }.hook {
                    before {
                        if (!isEnable) return@before
                        val reason = args().last().int()
                        if (reason == 10020 || reason == 10021) resultTrue()
                    }
                }
            }
    }
}