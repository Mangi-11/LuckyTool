package com.luckyzyx.luckytool.hook.scopes.filemanager

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveWordLimitForLabelNameFiles(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source BaseFileNameDialog
        dexKitBridge.findClass {
            matcher {
                methods {
                    add { name("onActivityResume") }
                    add { name("onTextChanged") }
                }
                usingStrings("BaseFileNameDialog")
            }
        }.apply {
            checkDataList("BaseFileNameDialog")

            findMethod {
                matcher {
                    paramCount(0)
                    returnType(Int::class.java)
                    usingNumbers(50)
                }
            }.apply {
                checkDataList("MaxCount")

                single().className.toClass().resolve().apply {
                    firstMethod {
                        name = single().methodName
                        emptyParameters()
                        returnType = Int::class
                    }.hook {
                        replaceTo(9999)
                    }
                }
            }
        }
    }
}