package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object SetAppUpdateDotDisplayMode : Hooker {

    private const val InstallSource = "com.android.server.pm.InstallSource"
    private const val OplusPMHelper = "com.android.server.pm.OplusOsPackageManagerHelper"

    override fun onHook() {
        val mode = prefs(ModulePrefs).getString("set_app_update_dot_display_mode", "0")
        if (mode == "0") return

        //Source PackageManagerServiceExtImpl
        "com.android.server.pm.PackageManagerServiceExtImpl".toClass().resolve().apply {
            firstMethod {
                name = "handleSuccessAtEndInHPPI"
                parameterCount = 6
            }.hook {
                after {
                    val packName = args(2).string()
                    val installSource = args(3).any()

                    val isUpdate = args(4).boolean()
                    val marketList = firstField {
                        name = "DEFAULT_MARKET_LIST";type = List::class
                    }.get<List<String>>() ?: java.util.ArrayList()

                    val installerPackageName = installSource as? String
                        ?: installSource?.asResolver()
                            ?.firstField { name = "mInstallerPackageName" }
                            ?.get<String>() ?: ""

                    if (isUpdate || marketList.contains(installerPackageName)) {
                        if (mode == "1") {
                            OplusPMHelper.toClass().resolve().firstMethod {
                                name = "addPkgToNotLaunchedList";parameters(String::class)
                            }.invoke(packName)
                        }
                    }
                }
            }
        }
    }
}