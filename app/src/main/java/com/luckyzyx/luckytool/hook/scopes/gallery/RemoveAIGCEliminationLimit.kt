package com.luckyzyx.luckytool.hook.scopes.gallery

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveAIGCEliminationLimit(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source EliminateDetectInfo / PanoramicSegmentationInfo
        dexKitBridge.findClass {
            matcher {
                addFieldForType(Boolean::class.java)
                addMethod { name("equals") }
                addMethod { name("hashCode") }
                addMethod { name("toString") }
                usingStrings("Info", "isContentSensitive")
            }
        }.apply {
            checkDataList("EliminateDetectInfo")

            single().name.toClass().resolve().apply {
                firstConstructor { parameters { it.contains(classOf<Boolean>()) } }.hook {
                    before {
                        args.forEachIndexed { index, it ->
                            if (it is Boolean) args(index).set(false)
                            if (it?.javaClass?.isEnum == true) args(index).set(null)
                        }
                    }
                }
            }
        }

        //Source EliminateStack / PanoramicSegmentationStack
        dexKitBridge.findClass {
            matcher {
                addFieldForType(Int::class.java)
                addFieldForType(String::class.java)
                addFieldForType(Boolean::class.java)
                addMethod { name("equals") }
                addMethod { name("hashCode") }
                addMethod { name("toString") }
                usingStrings("EliminateSaveEntry", "isContentSensitive")
            }
        }.apply {
            checkDataList("EliminateSaveEntry")

            single().name.toClass().resolve().apply {
                firstConstructor { parameters { it.contains(classOf<Boolean>()) } }.hook {
                    before {
                        args.forEachIndexed { index, it ->
                            if (it is Boolean) args(index).set(false)
                            if (it?.javaClass?.isEnum == true) args(index).set(null)
                        }
                        if (args.last() is Boolean) args().last().set(true)
                    }
                }
            }
        }
    }
}