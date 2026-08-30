package com.luckyzyx.luckytool.hook.scopes.notificationmanager

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.hook.core.get
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveNotificationManagerLimit : Hooker {
    override fun onHook() {
        //Source ControllerChannelGroup$AppItemListener -> 通知渠道列表允许通知锁
        "com.oplus.notificationmanager.property.uicontroller.ControllerChannelGroup\$AppItemListener".toClass()
            .resolve().apply {
                firstMethod { name = "isSwitchEnabled" }.hook {
                    replaceToTrue()
                }
            }
        //Source ControllerAllowNotificationChannel -> 通知渠道内允许通知锁
        "com.oplus.notificationmanager.property.uicontroller.ControllerAllowNotificationChannel".toClass()
            .resolve().apply {
                firstMethod { name = "isNormAppEnabled" }.hook {
                    replaceToTrue()
                }
            }
        //Source ControllerUnimportantChannel -> 通知渠道内不重要通知锁
        "com.oplus.notificationmanager.property.uicontroller.ControllerUnimportantChannel".toClass()
            .resolve().apply {
                firstMethod { name = "isNormAppEnabled" }.hook {
                    replaceToTrue()
                }
            }
        //Source ControllerAllowNotificationPkg -> 应用内允许通知锁
        "com.oplus.notificationmanager.property.uicontroller.ControllerAllowNotificationPkg".toClass()
            .resolve().apply {
                firstMethod { name = "isNormAppEnabled" }.hook {
                    replaceToTrue()
                }
            }
//
//        //Source ControllerAppLockBannerBubble -> 应用内气泡锁
//        "com.oplus.notificationmanager.property.uicontroller.ControllerAppLockBannerBubble".toClass()
//            .apply {
//                method { name = "isBubbleEnabled" }.hook {
//                    replaceToTrue()
//                }
//            }
//        //Source ControllerAppLockBannerBubble ->
//        "com.oplus.notificationmanager.property.uicontroller.ControllerChannelLockBannerBubble".toClass()
//            .apply {
//                method { name = "isBubbleEnabled" }.hook {
//                    replaceToTrue()
//                }
//            }
//        //Source ControllerAppLockBannerBubble ->
//        "com.oplus.notificationmanager.property.uicontroller.ControllerConversationLockBannerBubble".toClass()
//            .apply {
//                method { name = "isBubbleEnabled" }.hook {
//                    replaceToTrue()
//                }
//            }
    }
}