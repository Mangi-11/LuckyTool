package com.luckyzyx.luckytool.hook.scopes.settings

import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveDpiRestartRecovery(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        loadHooker(HookSettingsUtils(dexKitBridge))
    }

    @Obfuscate
    class HookSettingsUtils(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            //Source SettingsUtils
            dexKitBridge.findClass {
                matcher {
                    addMethod {
                        paramTypes(classOf<Context>(), classOf<Boolean>())
                    }
                    addMethod {
                        paramTypes(
                            classOf<String>(),
                            classOf<Int>(),
                            classOf<Int>(),
                            classOf<Boolean>()
                        )
                        usingStrings("restoreCompassPhoneDisplayDensity")
                    }
                    addMethod {
                        paramTypes(classOf<Context>(), classOf<String>(), classOf<Int>())
                        usingStrings("restorePhoneDisplayDensity")
                    }
                    usingStrings("SettingsUtils")
                }
            }.apply {
                checkDataList("RemoveDpiRestartRecovery Clazz")
                findMethod {
                    matcher {
                        paramTypes(classOf<Context>(), classOf<Boolean>())
                        addInvoke {
                            paramTypes(
                                classOf<String>(), classOf<Int>(),
                                classOf<Int>(), classOf<Boolean>()
                            )
                            usingStrings("restoreCompassPhoneDisplayDensity")
                        }
                        addInvoke {
                            paramTypes(classOf<Context>(), classOf<String>(), classOf<Int>())
                            usingStrings("restorePhoneDisplayDensity")
                        }
                    }
                }.apply {
                    checkDataList("RemoveDpiRestartRecovery Method")
                    single().className.toClass().resolve().apply {
                        firstMethod {
                            name = single().methodName
                            parameters(Context::class, Boolean::class)
                        }.hook {
                            intercept()
                        }
                    }
                }
            }
        }
    }
}