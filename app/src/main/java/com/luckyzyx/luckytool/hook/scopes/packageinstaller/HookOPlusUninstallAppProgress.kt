package com.luckyzyx.luckytool.hook.scopes.packageinstaller

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Button
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.MethodData

@Obfuscate
class HookOPlusUninstallAppProgress(val dexKitBridge: DexKitBridge) : Hooker {

    val autoDone = prefs(ModulePrefs).getBoolean("auto_click_uninstall_button", false)

    override fun onHook() {
        //Source OPlusUninstallAppProgress
        dexKitBridge.findClass {
            matcher {
                className("com.android.packageinstaller.oplus.OPlusUninstallAppProgress")
            }
        }.apply {
            checkDataList("OPlusUninstallAppProgress")

            val initView = findMethod {
                matcher {
                    paramCount(0)
                    returnType(Void.TYPE)
                    usingStrings("source_info", "package_name", "package_size")
                }
            }.checkDataList("initView").single()

            if (autoDone) hookAutoDone(initView)
        }
    }

    @SuppressLint("DiscouragedApi")
    fun hookAutoDone(initView: MethodData) {
        initView.className.toClass().resolve().apply {
            firstMethod { name = initView.methodName }.hook {
                after {
                    val activity = instance<Activity>()
                    activity.findViewById<Button>(
                        activity.resources.getIdentifier(
                            "complete_button", "id",
                            this@HookOPlusUninstallAppProgress.packageName
                        )
                    )?.performClick()
                }
            }
        }
    }
}