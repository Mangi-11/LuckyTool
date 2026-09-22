package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.scopes.packageinstaller.KeepAppDetailInstaller
import com.luckyzyx.luckytool.hook.scopes.packageinstaller.DisableStartAppDetail
import com.luckyzyx.luckytool.hook.scopes.packageinstaller.FixInstallButtonDisplayException
import com.luckyzyx.luckytool.hook.scopes.packageinstaller.HookInstallAppProgress
import com.luckyzyx.luckytool.hook.scopes.packageinstaller.HookOPlusUninstallAppProgress
import com.luckyzyx.luckytool.hook.scopes.packageinstaller.HookOplusPackageInstallerActivity
import com.luckyzyx.luckytool.hook.scopes.packageinstaller.HookUninstallerActivity
import com.luckyzyx.luckytool.hook.scopes.packageinstaller.ShowMoreApkPackageInformation
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookPackageInstaller : Hooker {
    override fun onHook() {

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            //Source FeatureOption
            dexKitBridge.findClass {
                matcher {
                    usingStrings(
                        "oppo.business.custom",
                        "com.oplus.packageinstaller.skip_appdetail",
                        "com.oplus.unknown_source_app_install"
                    )
                }
            }.singleOrNull() ?: return@create

            //Source OPlusPackageInstallerActivity
            loadHooker(HookOplusPackageInstallerActivity(dexKitBridge))

            //Source InstallAppProgress
            loadHooker(HookInstallAppProgress(dexKitBridge))

            //Source UninstallerActivity
            loadHooker(HookUninstallerActivity(dexKitBridge))

            //Source OPlusUninstallAppProgress
            loadHooker(HookOPlusUninstallAppProgress(dexKitBridge))

            //禁止启动AppDetail
            if (prefs(ModulePrefs).getBoolean("disable_start_app_detail", false)) {
                loadHooker(DisableStartAppDetail(dexKitBridge))
            } else if (android.os.Build.VERSION.SDK_INT >= 37) {
                loadHooker(KeepAppDetailInstaller(dexKitBridge))
            }
            //修复App安装页面底部按钮异常
            if (prefs(ModulePrefs).getBoolean("fix_install_button_display_exception", false)) {
                loadHooker(FixInstallButtonDisplayException)
            }
            //显示更多Apk包信息
            if (prefs(ModulePrefs).getBoolean("show_more_apk_package_information", false)) {
                loadHooker(ShowMoreApkPackageInformation(dexKitBridge))
            }
        }
    }
}