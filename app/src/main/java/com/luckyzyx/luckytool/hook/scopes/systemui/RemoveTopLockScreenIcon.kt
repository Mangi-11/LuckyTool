package com.luckyzyx.luckytool.hook.scopes.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveTopLockScreenIcon : YukiBaseHooker() {
    override fun onHook() {
        //Source LockIcon C14-
        "com.android.systemui.statusbar.phone.LockIcon".toClassOrNull()?.resolve()?.apply {
            firstMethod { name = "updateIconVisibility" }.hook {
                before {
                    firstArg().set(false)
                }
            }
        }

        //Source LockIconView C14 C15+
        val lockIconView = VariousClass(
            "com.android.keyguard.LockIconView",
            "com.android.keyguard.OplusLockIconView" //C16
        ).loadOrNull() ?: return

        lockIconView.resolve().apply {
            firstMethod { name = "updateColorAndBackgroundVisibility" }.hook {
                after {
                    firstField { name = "mLockIcon" }.of(instance).get<View>()?.isVisible = false
                }
            }
        }

        //Source LegacyLockIconViewController C15+
        "com.android.keyguard.LegacyLockIconViewController".toClassOrNull()?.resolve()?.apply {
            firstMethod { name { it.startsWith("updateVisibility") } }.hook {
                before {
                    firstField { type = lockIconView }.of(instance).get<View>()?.isVisible = false
                    result = null
                }
            }
        }
    }
}