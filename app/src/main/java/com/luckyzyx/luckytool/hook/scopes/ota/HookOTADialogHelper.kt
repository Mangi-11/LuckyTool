package com.luckyzyx.luckytool.hook.scopes.ota

import android.content.Context
import android.content.DialogInterface
import android.view.Window
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class HookOTADialogHelper(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        val autoDownload = prefs(ModulePrefs).getBoolean("remove_ota_auto_download_dialog", false)

        //Source OTADialogHelper
        dexKitBridge.findClass {
            matcher {
                addMethod { paramTypes(Window::class.java) }
                addMethod {
                    paramTypes(Context::class.java, DialogInterface.OnClickListener::class.java)
                }
                usingStrings("OTADialogHelper", "auto_download_network_type")
            }
        }.apply {
            checkDataList("OTADialogHelper")

            if (autoDownload) findMethod {
                matcher {
                    returnType(Void.TYPE)
                    usingStrings("auto_download_network_type")
                    //R.array.data_network_switch_array
                }
            }.apply {
                checkDataList("AutoDownloadDialog")

                single().className.toClass().resolve().apply {
                    firstMethod {
                        name = single().methodName
                        returnType = Void.TYPE
                    }.hook {
                        intercept()
                    }
                }
            }
        }
    }
}