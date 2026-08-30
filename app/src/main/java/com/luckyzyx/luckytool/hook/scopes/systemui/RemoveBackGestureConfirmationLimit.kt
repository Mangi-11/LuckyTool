package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveBackGestureConfirmationLimit : Hooker {
    override fun onHook() {
        //Source SideGestureDetector
        "com.oplus.systemui.navigationbar.gesture.sidegesture.SideGestureDetector".toClass()
            .resolve().apply {
            firstMethod {
                name = "shouldRespondToGesture"
                emptyParameters()
                returnType = Boolean::class
            }.hook {
                before {
                    firstField {
                        name = "mIsExitMisTouchPreventionFlag"
                        type = Boolean::class
                    }.of(instance).set(true)
                }
            }
            firstMethod {
                name = "shouldInjectToGestureMode"
                emptyParameters()
                returnType = Boolean::class
            }.hook {
                before {
                    firstField {
                        name = "mIsFirstGestureInGameMode"
                        type = Boolean::class
                    }.of(instance).set(false)
                }
            }
        }
    }
}