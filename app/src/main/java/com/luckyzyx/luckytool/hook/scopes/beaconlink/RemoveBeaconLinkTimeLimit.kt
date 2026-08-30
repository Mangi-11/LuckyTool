package com.luckyzyx.luckytool.hook.scopes.beaconlink

import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveBeaconLinkTimeLimit(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source IDs.java
        dexKitBridge.findClass {
            matcher {
                fields {
                    add {
                        type(String::class.java)
                        addReadMethod {
                            returnType(HashMap::class.java)
                        }
                    }
                    add {
                        type(Long::class.java)
                        addWriteMethod {
                            paramTypes(Context::class.java, String::class.java, String::class.java)
                        }
                        addReadMethod {
                            paramTypes(Context::class.java, String::class.java, String::class.java)
                        }
                    }
                }
                fieldCount(2)
            }
        }.apply {
            checkDataList("IDs")

            single().name.toClass().resolve().apply {
                firstMethodOrNull {
                    parameters(String::class)
                    returnType = Boolean::class
                }?.hook {
                    replaceToTrue()
                } ?: run {
                    firstConstructor {
                        parameters(String::class, Long::class)
                    }.hook {
                        before {
                            args().last().set(0L)
                        }
                    }
                }
            }
        }
    }
}