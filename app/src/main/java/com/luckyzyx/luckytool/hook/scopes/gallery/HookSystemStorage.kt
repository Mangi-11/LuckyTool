package com.luckyzyx.luckytool.hook.scopes.gallery

import android.annotation.SuppressLint
import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.VagueType
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.XLog
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.getOSVersionCode
import com.luckyzyx.luckytool.utils.isZh
import com.luckyzyx.luckytool.utils.safeOf
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class HookSystemStorage(val dexKitBridge: DexKitBridge) : Hooker {

    override fun onHook() {
        val osCode = getOSVersionCode

        //替换OnePlus机型水印
        val notOplus = prefs(ModulePrefs).getBoolean("replace_oneplus_model_watermark", false)
        //高级筛选
        val seniorPicked =
            prefs(ModulePrefs).getBoolean("enable_photo_listview_senior_picked", false)
        //拇指线
        val thumbLine = prefs(ModulePrefs).getString("set_photo_view_thumb_line_display_mode", "0")
        //GIF合成
        val gifSynthesis = prefs(ModulePrefs).getBoolean("enable_photo_editor_gif_synthesis", false)
        //闪速抠图
        val lnsImage = prefs(ModulePrefs).getBoolean("enable_lns_cut_photo", false)
        //画框水印
        val frameWaterMark = prefs(ModulePrefs).getBoolean("enable_frame_watermark", false)
        //哈苏水印
        val hasselWaterMark = prefs(ModulePrefs).getBoolean("enable_hassel_watermark", false)
        //AI大师水印
        val aiWaterMark = prefs(ModulePrefs).getBoolean("enable_ai_master_watermark", false)
        //隐私水印
        val privicyWaterMark = prefs(ModulePrefs).getBoolean("enable_privacy_watermark", false)
        //新春水印
        val springFestival =
            prefs(ModulePrefs).getBoolean("enable_spring_festival_watermark", false)
        //国庆水印
        val nationalDay =
            prefs(ModulePrefs).getBoolean("enable_national_day_watermark", false)

        //Source OtherSystemStorage
        dexKitBridge.findClass {
            matcher {
                fields {
                    addForType(Context::class.java)
                }
                methods {
                    add { paramCount(2);returnType(Int::class.javaObjectType) }
                    add { paramCount(2);returnType(Long::class.javaObjectType) }
                    add { paramCount(2);returnType(Boolean::class.javaObjectType) }
                    add { paramCount(2);returnType(String::class.java) }
                    add { paramCount(2);returnType(Void.TYPE) }
                    add { paramCount(0);returnType(Boolean::class.java) }
//                    add { paramCount(4);returnType(Boolean::class.java) }
                }
                usingStrings("configNode")
            }
        }.apply {
            checkDataList("HookSystemStorage")
            single().name.toClass().resolve().apply {
                firstMethod {
                    parameters(VagueType, Boolean::class)
                    returnType = Boolean::class.javaObjectType
                }.hook {
                    after {
                        val context = firstField { type = Context::class }.of(instance)
                            .get<Context>() ?: return@after
                        val configNode = args().first().any().toString()
                        when {
                            //com.oplus.camera.support.custom.hasselblad.watermark
                            configNode.contains("feature_is_support_watermark") -> {
                                if (osCode < 30 && hasselWaterMark) resultTrue()
                                if (osCode >= 34 && aiWaterMark) resultTrue()
                            }
                            //com.oplus.camera.support.custom.hasselblad.watermark
                            configNode.contains("feature_is_support_hassel_watermark") -> if (hasselWaterMark) resultTrue()
                            //com.oplus.feature.custom.makeup.watermark.support
//                            configNode.contains("feature_is_support_street_watermark") -> if (waterMark) resultTrue()
                            //debug.enable.ipu
//                            configNode.contains("feature_is_support_ipu_watermark") -> if (waterMark) resultTrue()
                            //com.oplus.camera.support.custom.color.watermark
//                            configNode.contains("feature_is_support_color_watermark") -> if (waterMark) resultTrue()
                            //com.oplus.camera.support.custom.lonely.planet.watermark
//                            configNode.contains("feature_is_support_lonely_planet_watermark") -> if (waterMark) resultTrue()

                            //isRegionIndia / isInTime / is_realme_brand
//                            configNode.contains("feature_is_support_diwali_festival_watermark") -> if (waterMark) resultTrue()

                            //is_realme_brand / debug.gallery.photo.editor.watermark.switcher
//                            configNode.contains("feature_is_support_photo_editor_watermark") -> if (hasselWaterMark) resultTrue()
                            //first_api_level
//                            configNode.contains("feature_is_support_photo_editor_frame_watermark") -> result =
//                                frameWaterMark

                            //is_realme_brand / debug.gallery.photo.editor.watermark.switcher
                            configNode.contains("feature_is_support_privacy_watermark") -> if (privicyWaterMark) resultTrue()
                            //os.graphic.gallery.photolistview.senior_picked
                            configNode.contains("feature_is_support_senior_picked") -> if (seniorPicked) resultTrue()
                            //debug.gallery.photo.photothumbline / os.graphic.gallery.photoview.thumb_line
                            configNode.contains("feature_is_support_photo_thumb_line") -> when (thumbLine) {
                                "1" -> resultTrue()
                                "2" -> resultFalse()
                            }
                            //debug.gallery.gif.support.synthesis / os.graphic.gallery.photoeditor.gif_synthesis
                            configNode.contains("feature_is_support_gif_synthesis") -> if (gifSynthesis) resultTrue()
                            //debug.gallery.lns / os.graphic.gallery.photoview.lns
                            configNode.contains("feature_is_support_lns") -> if (lnsImage) resultTrue()

                            //isOsSupport 29 -> 27 (Downgrade)
                            //debug.gallery.photo.editor.watermark.switcher / is_region_cn (property_domestic)
                            configNode.contains("feature_is_support_spring_festival_watermark") -> {
                                if (springFestival && osCode in 27..33 && isRegionCN(context)) resultTrue()
                            }
                            //debug.gallery.photo.editor.watermark.switcher / is_region_cn (property_domestic)
                            configNode.contains("feature_is_support_national_day_watermark") -> {
                                if (nationalDay && osCode in 27..33 && isRegionCN(context)) resultTrue()
                            }
                        }
                    }
                }
            }
        }

        if (hasselWaterMark) {
            //Source WatermarkInfo / WatermarkDevice
            dexKitBridge.findClass {
                matcher {
                    addFieldForType(Int::class.java)
                    addFieldForType(Boolean::class.java)
                    addFieldForType(String::class.java)
                    usingStrings("WatermarkDevice", "isHasselDevice")
                }
            }.apply {
                checkDataList("WatermarkDevice HasselDevice", onlyOne = false)
                forEachIndexed { _, classData ->
                    classData.name.toClass().resolve().apply {
                        firstFieldOrNull { type = Boolean::class }?.let {
                            constructor { }.hookAll {
                                after {
                                    it.copy().of(instance).set(true)
                                }
                            }
                        } ?: run {
                            XLog.debug("WatermarkDevice HasselDevice hook error! -> ${classData.name}")
                        }
                    }
                }
            }
        }

        //Source ConfigAbilityImpl
        dexKitBridge.findClass {
            matcher {
//                fields {
//                    addForType(Context::class.java)
//                }
                methods {
                    add { name = "close";paramCount(0) }
                    add { name = "contains";paramTypes(String::class.java) }
                    add { returnType(AutoCloseable::class.java) }
                    add {
//                        paramTypes(String::class.java, Int::class.java)
                        paramCount(2)
                        returnType(Int::class.javaObjectType)
                    }
                    add {
//                        paramTypes(String::class.java, Long::class.java)
                        paramCount(2)
                        returnType(Long::class.javaObjectType)
                    }
                    add {
                        paramTypes(String::class.java, String::class.java)
                        returnType(String::class.java)
                    }
//                    add {
//                        paramTypes(String::class.java, Boolean::class.java)
//                        paramCount(2)
//                        returnType(Boolean::class.javaObjectType)
//                    }
                }
                usingStrings("ConfigAbilityImpl")
            }
        }.apply {
            checkDataList("HookConfigAbility")
            single().name.toClass().resolve().apply {
                firstMethod {
                    parameters(String::class, Boolean::class)
                    returnType = Boolean::class.javaObjectType
                }.hook {
                    after {
                        when (args().first().string()) {
                            "is_oneplus_brand" -> if (notOplus) resultFalse()
//                            "feature_is_support_watermark" -> if (waterMark) resultTrue()
//                            "feature_is_support_hassel_watermark" -> if (hasselWaterMark) resultTrue()
//                            "feature_is_support_photo_editor_watermark" -> if (waterMark) resultTrue()
//                            "feature_is_support_privacy_watermark" -> if (privicyWaterMark) resultTrue()
//                            "feature_is_support_senior_picked" -> if (seniorPicked) resultTrue()
//                            "feature_is_support_photo_thumb_line" -> when (thumbLine) {
//                                "1" -> resultTrue()
//                                "2" -> resultFalse()
//                            }
//
//                            "feature_is_support_gif_synthesis" -> if (gifSynthesis) resultTrue()
//                            "feature_is_support_lns" -> if (lnsImage) resultTrue()

//                            photopage_detail_ic_dolby_vision
//                            "brighten_version_dolby" -> if (gifSynthesis) resultTrue()
//                            "feature_is_support_dolby_brighten" -> if (gifSynthesis) resultTrue()
//                            "is_support_dolby_decode" -> if (gifSynthesis) resultTrue()
//                            "is_support_dolby_encode" -> if (gifSynthesis) resultTrue()
//                            "is_support_dolby_encode_accelerate" -> if (gifSynthesis) resultTrue()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    fun isRegionCN(context: Context): Boolean {
        return safeOf(isZh(context)) {
            context.resources.getBoolean(
                context.resources.getIdentifier(
                    "property_domestic", "bool", this@HookSystemStorage.packageName
                )
            )
        }
    }
}