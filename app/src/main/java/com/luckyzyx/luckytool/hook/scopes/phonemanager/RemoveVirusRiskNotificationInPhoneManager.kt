package com.luckyzyx.luckytool.hook.scopes.phonemanager

import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveVirusRiskNotificationInPhoneManager(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source VirusScanNotifyListener
        dexKitBridge.findClass {
            matcher {
                fields {
                    addForType(Context::class.java)
                    addForType(String::class.java)
                }
                methods {
                    add { paramTypes(ArrayList::class.java) }
                    add { returnType(Int::class.java) }
                    add { returnType(String::class.java) }
                }
                usingStrings("VirusScanNotifyListener")
            }
        }.apply {
            checkDataList("RemoveVirusRiskNotificationInPhoneManager")
            single().name.toClass().resolve().apply {
                method { parameters(ArrayList::class) }.hookAll {
                    intercept()
                }
            }
        }
    }
}