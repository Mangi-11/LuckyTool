package com.luckyzyx.luckytool.hook.scopes.packageinstaller

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.view.View
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
class HookOplusPackageInstallerActivity(val dexKitBridge: DexKitBridge) : Hooker {

    val disableScan = prefs(ModulePrefs).getBoolean("skip_apk_scan", false)
    val allowReplace = prefs(ModulePrefs).getBoolean("allow_downgrade_install", false)
    val autoInstall = prefs(ModulePrefs).getBoolean("auto_click_install_button", false)

    override fun onHook() {
        //Source OPlusPackageInstallerActivity
        dexKitBridge.findClass {
            matcher {
                className("com.android.packageinstaller.oplus.OPlusPackageInstallerActivity")
            }
        }.apply {
            checkDataList("OPlusPackageInstallerActivity")

            val checkToScanRisk = findMethod {
                matcher {
                    paramCount(0)
                    usingFields {
                        add { type(Long::class.java) }
                        add { type(Boolean::class.java) }
                        add { type(View::class.java) }
                    }
                    addCaller { name("onClick") }
                    addInvoke { paramCount(0);returnType(Void.TYPE) }
                    usingNumbers(8)
                }
            }.checkDataList("checkToScanRisk").single()
            val initiateInstall = findMethod {
                matcher {
                    paramCount(0)
                    usingFields {
                        add { type(PackageManager::class.java) }
                        add { type(PackageInfo::class.java) }
                        add { type(ApplicationInfo::class.java) }
                        add { type(Boolean::class.java) }
                    }
                    addCaller { name("onClick") }
                    addInvoke { paramCount(0);returnType(Void.TYPE) }
                    usingNumbers(3, 8388608, 8192)
                }
            }.checkDataList("initiateInstall").single()

            val parseReplaceInstall = findMethod {
                matcher {
                    paramCount(0)
                    usingStrings("currentVersionCode", "apkVersioncode")
                }
            }.checkDataList("parseReplaceInstall").single()
            val preSafeInstall = findMethod {
                matcher {
                    paramCount(0)
                    usingStrings("startAppdetail", "reason")
                }
            }.checkDataList("parseReplaceInstall").single()

            val startInstallConfirm = findMethod {
                matcher {
                    paramCount(0)
                    usingNumbers(0)
                    addCaller { name(initiateInstall.name);paramCount(0) }
                    addInvoke { paramCount(0);returnType(Void.TYPE) }
                    addUsingField { type(View::class.java) }
                    addUsingField { type(Boolean::class.java) }
                    addUsingField { type(ArrayList::class.java) }
                }
            }.checkDataList("startInstallConfirm").single()

            if (disableScan) hookSkipScanRisk(checkToScanRisk, initiateInstall)
            if (allowReplace) hookAllowReplace(parseReplaceInstall, preSafeInstall)
            if (autoInstall) hookAutoClickOk(startInstallConfirm)
        }
    }

    fun hookSkipScanRisk(checkToScanRisk: MethodData, initiateInstall: MethodData) {
        checkToScanRisk.className.toClass().resolve().apply {
            firstMethod { name = checkToScanRisk.methodName }.hook {
                before {
                    firstMethod { name = initiateInstall.methodName }.of(instance).invoke()
                    resultNull()
                }
            }
        }
    }

    fun hookAllowReplace(parseReplaceInstall: MethodData, preSafeInstall: MethodData) {
        parseReplaceInstall.className.toClass().resolve().apply {
            firstMethod { name = parseReplaceInstall.methodName }.hook {
                before {
                    firstMethod { name = preSafeInstall.methodName }.of(instance).invoke()
                    resultNull()
                }
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    fun hookAutoClickOk(startInstallConfirm: MethodData) {
        startInstallConfirm.className.toClass().resolve().apply {
            firstMethod { name = startInstallConfirm.methodName }.hook {
                after {
                    val activity = instance<Activity>()
                    activity.findViewById<Button>(
                        activity.resources.getIdentifier(
                            "ok_button", "id",
                            this@HookOplusPackageInstallerActivity.packageName
                        )
                    ).performClick()
                }
            }
        }
    }
}