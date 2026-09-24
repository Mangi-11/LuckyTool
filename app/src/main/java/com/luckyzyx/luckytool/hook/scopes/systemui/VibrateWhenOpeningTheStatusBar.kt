package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object VibrateWhenOpeningTheStatusBar : YukiBaseHooker() {
    override fun onHook() {
        //Source PanelViewController -> config_vibrateOnIconAnimation
        VariousClass(
            "com.android.systemui.statusbar.phone.PanelViewController", //C13
            "com.android.systemui.shade.NotificationPanelViewController" //C14
        ).toClass().resolve().apply {
            firstConstructor().hook {
                after {
                    firstField { name = "mVibrateOnOpening" }.of(instance).set(true)
                }
            }
        }

        //Source StatusBarCommandQueueCallbacks -> config_vibrateOnIconAnimation
        VariousClass(
            "com.android.systemui.statusbar.phone.StatusBarCommandQueueCallbacks", //C13
            "com.android.systemui.statusbar.phone.CentralSurfacesCommandQueueCallbacks" //C14
        ).toClass().resolve().apply {
            firstFieldOrNull { name = "mVibrateOnOpening" }?.let {
                firstConstructor().hook {
                    after {
                        it.copy().of(instance).set(true)
                    }
                }
            }
        }

        //Source PanelViewController -> config_vibrateOnIconAnimation
        "com.android.systemui.statusbar.phone.StatusBar".toClassOrNull()?.resolve()?.apply {
            firstFieldOrNull { name = "mVibrateOnOpening" }?.let {
                firstMethod { name = "start" }.hook {
                    after {
                        it.copy().of(instance).set(true)
                    }
                }
            }
        }
    }
}