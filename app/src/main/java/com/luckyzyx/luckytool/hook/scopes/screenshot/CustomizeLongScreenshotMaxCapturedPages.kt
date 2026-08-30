package com.luckyzyx.luckytool.hook.scopes.screenshot

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.VagueType
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class CustomizeLongScreenshotMaxCapturedPages(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source ScrollCaptureConfigs -> scroll_configs_max_captured_pages / scroll_configs_max_captured_pixels
        //Source StitchLimitUtils -> isCapturedPagesReachLimit / trimToStitchLimit
        dexKitBridge.findClass {
            matcher {
                fieldCount(0)
                methods {
                    add { returnType(Int::class.java) }
                    add { returnType(Boolean::class.java) }
                    add {
                        paramTypes(Int::class.java, Int::class.java)
                        returnType(Int::class.java)
                    }
                }
                usingStrings("StitchLimitUtils")
            }
        }.apply {
            checkDataList("CustomizeLongScreenshotMaxCapturedPages")
            single().name.toClass().resolve().apply {
                //isCapturedPagesReachLimit
                firstMethod {
                    parameters(VagueType, Int::class)
                    parameterCount = 2
                    returnType = Boolean::class
                }.hook {
                    replaceToFalse()
                }
                //trimToStitchLimit
                firstMethod {
                    parameters(VagueType, Int::class, Int::class)
                    parameterCount = 3
                    returnType = Int::class
                }.hook {
                    replaceTo(-1)
                }
            }
        }
    }
}