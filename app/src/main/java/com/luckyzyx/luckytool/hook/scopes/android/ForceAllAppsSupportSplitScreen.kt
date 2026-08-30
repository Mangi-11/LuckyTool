package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ForceAllAppsSupportSplitScreen : Hooker {
    override fun onHook() {
        var isEnable = prefs(ModulePrefs).getBoolean("force_all_apps_support_split_screen", false)
        dataChannel.wait<Boolean>("force_all_apps_support_split_screen") { isEnable = it }

        //Source OplusSplitScreenManagerService
        "com.android.server.wm.OplusSplitScreenManagerService".toClass().resolve().apply {
            method {
                name = "supportsSplitScreenByVendorPolicy"
                parameters { it[0] == classOf<String>() && it[1] == classOf<String>() }
                parameterCount { it in 3..4 }
            }.hookAll {
                before {
                    if (!isEnable) return@before
                    val packageName = args().first().string()
                    val activityName = args(1).string()
//                    val candidate = args(2).boolean()

                    if (packageName.isBlank()) return@before

                    val isSafeSenterUI = firstMethod {
                        name = "isSafeSenterUI"
                        parameterCount = 1
                    }.of(instance).invoke<Boolean>(activityName) ?: false
                    if (isSafeSenterUI) return@before

                    if (method.parameterCount == 4) {
                        val userId = args().last().int()
                        val isHidenPackage = firstMethod {
                            name = "isHidenPackage"
                            parameterCount = 2
                        }.of(instance).invoke<Boolean>(packageName, userId) ?: false
                        if (isHidenPackage) return@before
                    }

                    resultTrue()
                }
            }
            firstMethod { name = "isInForbidActivityList" }.hook {
                if (isEnable) replaceToFalse()
            }
            firstMethod { name = "supportsSplitScreenWindowingMode" }.hook {
                if (isEnable) replaceToTrue()
            }
        }
    }
}