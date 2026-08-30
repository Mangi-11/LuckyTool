package com.luckyzyx.luckytool.hook.scopes.games

import android.os.Bundle
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.result
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveRootCheck(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source COSASDKManager
        //Search getSupportCoolEx new Bundle -> Class
        //Search getFeature -> dynamic_feature_cool_ex
        //isSafe:null; -> isSafe:0
        dexKitBridge.findClass {
            matcher {
                fields {
//                    addForType(String::class.java)
                    addForType(Boolean::class.java)
                    addForType(Int::class.java)
                }
                methods {
//                    add { name = "clear"; paramCount(0) }
                    add { paramCount(0); returnType(Bundle::class.java) }
                }
                usingStrings("COSASDKManager")
            }
        }.apply {
            checkDataList("RemoveRootCheck")
            single().name.toClass().resolve().apply {
                firstMethod {
                    emptyParameters()
                    returnType = Bundle::class
                }.hook {
                    after {
                        result<Bundle>()?.putInt("isSafe", 0)
                    }
                }
            }
        }
    }
}
