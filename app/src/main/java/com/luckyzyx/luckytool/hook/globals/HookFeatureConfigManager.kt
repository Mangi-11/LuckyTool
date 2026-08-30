package com.luckyzyx.luckytool.hook.globals

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.hook.core.result
import com.luckyzyx.luckytool.hook.core.get
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class HookFeatureConfigManager(private val features: Map<String, Boolean>) : Hooker {
    override fun onHook() {
        if (features.isEmpty()) return

        //Source OplusFeatureConfigManager
        "com.oplus.content.OplusFeatureConfigManager".toClassOrNull()?.resolve()?.apply {
            firstMethod {
                name = "hasFeature"
                parameters(String::class)
                returnType = Boolean::class
            }.hook {
                before {
                    val key = args().first().cast<String>()
                    if (key.isNullOrBlank()) return@before
                    val value = features[key]
                    if (value != null) result = value
                }
            }
        }

        if (packageName != "android") return

        //Source OplusFeatureConfigManager -> OplusFeatureConfigManagerService
        "com.android.server.content.OplusFeatureConfigManagerService".toClassOrNull()?.resolve()
            ?.apply {
                firstMethod {
                    name = "hasFeature"
                    parameters(String::class)
                    returnType = Boolean::class
                }.hook {
                    before {
                        val key = args().first().string()
                        if (key.isBlank()) return@before
                        val value = features[key]
                        if (value != null) result = value
                    }
                }
                firstMethodOrNull {
                    name = "hasFeatureMap"
                    parameters(String::class, Int::class)
                    returnType = Boolean::class
                }?.hook {
                    before {
                        val key = args().first().string()
                        if (key.isBlank()) return@before
                        val value = features[key]
                        if (value != null) result = value
                    }
                }
            }
    }
}