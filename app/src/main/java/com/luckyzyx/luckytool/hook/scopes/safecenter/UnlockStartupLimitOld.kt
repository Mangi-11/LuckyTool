package com.luckyzyx.luckytool.hook.scopes.safecenter

import android.content.Context
import android.content.pm.ApplicationInfo
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class UnlockStartupLimitOld(val dexKitBridge: DexKitBridge) : Hooker {

    override fun onHook() {
        //Source StartupManager.java
        //Search -> auto_start_max_allow_count -> update max allow count
        dexKitBridge.findClass {
            matcher {
                fields {
                    addForType(Int::class.java)
                    addForType(Any::class.java)
                    addForType(Map::class.java)
                    addForType(Boolean::class.java)
                    addForType(Context::class.java)
                }
                methods {
                    add { paramTypes(List::class.java) }
                    add { paramTypes(String::class.java) }
                    add { returnType(Void.TYPE) }
                    add { returnType(List::class.java) }
                    add { returnType(Boolean::class.java) }
                    add { returnType(ApplicationInfo::class.java) }
                }
                usingStrings("StartupManager")
            }
        }.apply {
            checkDataList("UnlockStartupLimitOld")
            single().name.toClass().resolve().apply {
                method {
                    parameters(Context::class)
                    returnType = Void.TYPE
                }.hookAll {
                    after {
                        firstField { type = Int::class }.set(999)
                    }
                }
            }
        }
    }
}