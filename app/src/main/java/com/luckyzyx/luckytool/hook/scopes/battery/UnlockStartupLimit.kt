package com.luckyzyx.luckytool.hook.scopes.battery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class UnlockStartupLimit(val dexKitBridge: DexKitBridge) : Hooker {

    override fun onHook() {
        val recordDatabase = "com.oplus.startupapp.data.database.RecordDatabase"

        //Source StartupManager.java
        //Search -> ? 5 : 20; -> Method
        dexKitBridge.findMethod {
            matcher {
                declaredClass {
                    addFieldForType(Context::class.java)
                    addFieldForType(recordDatabase)
                    usingStrings("StartupManager")
                    addMethod { paramCount(0);returnType(Int::class.java) }
                    addMethod { paramTypes(Intent::class.java);returnType(Void.TYPE) }
                    addMethod { paramTypes(Bundle::class.java);returnType(Void.TYPE) }
                }
                paramCount(0)
                returnType(Int::class.java)
                usingNumbers(5, 20)
            }
        }.apply {
            checkDataList("UnlockStartupLimit")
            single().className.toClass().resolve().apply {
                firstMethod {
                    name = single().methodName
                    emptyParameters()
                    returnType = Int::class
                }.hook {
                    replaceTo(999)
                }
            }
        }
    }
}