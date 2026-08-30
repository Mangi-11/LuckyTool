package com.luckyzyx.luckytool.hook.scopes.camera

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveCameraFlashLimit(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        loadHooker(HookLowPowerFlashLimit(dexKitBridge))
    }

    @Obfuscate
    class HookLowPowerFlashLimit(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            //Source CameraManager
            dexKitBridge.findClass {
                matcher {
                    className("com.oplus.camera.CameraManager")
                }
            }.apply {
                checkDataList("RemoveCameraFlashLimit Clazz")
                findMethod {
                    matcher {
                        paramTypes(Int::class.java)
                        returnType(Void.TYPE)
                        usingNumbers(15, 5, 2)
                    }
                }.apply {
                    checkDataList("RemoveCameraFlashLimit Method")
                    single().className.toClass().resolve().apply {
                        firstMethod {
                            name = single().methodName
                            parameters(Int::class)
                            returnType = Void.TYPE
                        }.hook {
                            before {
                                args().first().set(100)
                            }
                        }
                    }
                }
            }
        }
    }
}