package com.luckyzyx.luckytool.hook.scopes.systemui

import android.content.Context
import android.provider.Settings
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import com.luckyzyx.luckytool.hook.utils.SettingsUtils
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ForceEnableScreenOffMusicSupport : Hooker {
    override fun onHook() {
        val statisticUtil = VariousClass(
            "com.oplusos.systemui.notification.util.NotificationStatisticUtil",  //C13
            "com.oplus.systemui.aod.mediapanel.util.AodMediaStatisticUtil"  //C14
        )

        //Source OplusBlackScreenGestureControllExImpl
        VariousClass(
            "com.oplus.systemui.keyguard.OplusBlackScreenGestureControllExImpl", //C13
            "com.oplus.systemui.keyguard.gesture.OplusBlackScreenGestureControllExImpl" //C14
        ).toClass().resolve().apply {
            (firstMethodOrNull { name = "resetAodMediaSupportConfig" }
                ?: firstMethod { name = "init" }).hook {
                after {
                    val context = firstField { name = "mContext" }.of(instance).get<Context>()
                        ?: return@after
                    SettingsUtils.putIntForUser(
                        Settings.Secure::class.java, context.contentResolver,
                        "aod_media_support", 1, 0
                    )
                    statisticUtil.toClass().resolve().firstMethod {
                        name = "setAodMediaSupport"
                        parameterCount = 1
                    }.invoke(true)
                }
            }
        }
    }
}