package com.luckyzyx.luckytool.hook.scopes.launcher

import com.highcapable.kavaref.extension.classOf
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hookMethod
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class ForceEnableRecentTaskMemoryDisplay(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source MemoryInfoManager
        dexKitBridge.findClass {
            matcher {
                usingStrings("MemoryInfoManager")
            }
        }.apply {
            checkDataList("MemoryInfoManager")

            findMethod {
                matcher {
                    paramCount(0)
                    returnType(classOf<Boolean>())
                    callerMethods {
                        add { name("getIsMem") }
                        add {
                            paramTypes(classOf<Boolean>())
                            returnType(Void.TYPE)
                            usingNumbers(8)
                        }
                    }
                }
            }.apply {
                checkDataList("needMemoryDetail")
            }.single().getMethodInstance(appClassLoader).hookMethod {
                replaceToTrue()
            }

            findMethod {
                matcher {
                    paramCount(0)
                    returnType(classOf<Boolean>())
                    callerMethods {
                        add {
                            paramTypes(classOf<Boolean>())
                            returnType(Void.TYPE)
                            usingStrings("OplusRecentsView")
                        }
                    }
                }
            }.apply {
                checkDataList("isAllowMemoryInfoDisplay")
            }.single().getMethodInstance(appClassLoader).hookMethod {
                replaceToTrue()
            }
        }
    }
}