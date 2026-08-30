package com.luckyzyx.luckytool.hook.scopes.camera

import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.VagueType
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.hook.core.result
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class CustomModelWaterMark(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        val waterMark = prefs(ModulePrefs).getString("custom_model_watermark", "None")
        if (waterMark.isBlank() || waterMark == "None") return

        //Source BaseWatermarkPresenter / BaseWatermarkCreator
        dexKitBridge.findMethod {
            matcher {
                paramTypes(Context::class.java, Float::class.java, null, null)
                usingStrings(
                    "key_watermark_part_a_line", "key_watermark_part_b_line"
                )
                usingNumbers(0.03F, 0.007F)
            }
        }.apply {
            checkDataList("CustomModelWaterMark Shot")
            single().className.toClass().resolve().apply {
                firstMethod {
                    name(single().methodName)
                    parameters(Context::class, Float::class, VagueType, VagueType)
                    returnType(single().returnTypeName)
                }.hook {
                    after {
                        val hashMap = result<HashMap<String, Any>>() ?: return@after
                        hashMap["key_watermark_part_a_line"]?.apply {
                            firstField { type = ArrayList::class }.of(this)
                                .get<ArrayList<String>>()?.apply {
                                    replaceAll { if (it.contains("Shot on OnePlus")) waterMark else it }
                                }
                        }
                        hashMap["key_watermark_part_b_line"]?.apply {
                            firstField { type = ArrayList::class }.of(this)
                                .get<ArrayList<String>>()?.apply {
                                    replaceAll { if (it.contains("Shot on OnePlus")) waterMark else it }
                                }
                        }
                    }
                }
            }
        }

        //Source MarketUtil / MarketNameInfo
        dexKitBridge.findMethod {
            matcher {
                paramCount(0)
                returnType(String::class.java)
                usingStrings(
                    "", "ro.vendor.oplus.market.enname", "ro.vendor.oplus.market.name"
                )
            }
        }.apply {
            checkDataList("CustomModelWaterMark MarketName", false)
            forEach {
                it.className.toClass().resolve().apply {
                    firstMethod {
                        name = it.methodName
                        emptyParameters()
                        returnType = String::class
                    }.hook {
                        replaceTo(waterMark)
                    }
                }
            }
        }

        //Source WatermarkHelper / WatermarkSingleton
        dexKitBridge.findMethod {
            matcher {
                returnType(String::class.java)
                usingStrings("[\u4e00-\u9fa5]", "")
            }
        }.apply {
            checkDataList("CustomModelWaterMark RemoveChineseOfString")
            single().className.toClass().resolve().apply {
                method {
                    name = single().methodName
                    returnType = String::class
                }.hookAll {
                    replaceTo(waterMark)
                }
            }
        }
    }
}