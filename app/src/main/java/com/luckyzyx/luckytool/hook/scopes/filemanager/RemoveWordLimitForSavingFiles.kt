package com.luckyzyx.luckytool.hook.scopes.filemanager

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveWordLimitForSavingFiles(val dexKitBridge: DexKitBridge) : YukiBaseHooker() {
    override fun onHook() {
        //Source ActionModeController
        dexKitBridge.findClass {
            matcher {
                className("com.oplus.filemanager.picker.controller.ActionModeController")
            }
        }.apply {
            checkDataList("ActionModeController")

            findField {
                matcher {
                    type(classOf<Int>())
                    addReadMethod {
                        paramCount(0)
                        returnType(Void.TYPE)
                    }
                    addReadMethod {
                        paramCount(4)
                        returnType(Void.TYPE)
                    }
                }
            }.apply {
                checkDataList("MaxCount")

                single().className.toClass().resolve().apply {
                    firstConstructor { parameterCount = 1 }.hook {
                        after {
                            firstField { name = single().fieldName; type = Int::class }.of(instance)
                                .set(9999)
                        }
                    }
                }
            }
        }
    }
}