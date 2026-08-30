package com.luckyzyx.luckytool.hook.scopes.filemanager

import android.text.InputFilter
import android.widget.EditText
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveWordLimitForCompressFiles(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source CompressConfirmDialog
        dexKitBridge.findClass {
            matcher {
                methods {
                    add { name("onTextChanged") }
                    add { paramTypes(EditText::class.java, InputFilter::class.java) }
                }
                usingStrings("CompressConfirmDialog")
            }
        }.apply {
            checkDataList("CompressConfirmDialog")

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