package com.luckyzyx.luckytool.hook.scopes.market

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge
import java.util.concurrent.atomic.AtomicBoolean

@Obfuscate
class RemoveMarketSplashPageAppRecommend(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        val isV4 = "com.heytap.cdo.splash.domain.dto.v4.SplashDtoV4".toClassOrNull() != null
        if (isV4) loadHooker(MarketSplashPageV4(dexKitBridge))
        else loadHooker(MarketSplashPageV2(dexKitBridge))
    }

    @Obfuscate
    class MarketSplashPageV4(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            val splashDto = "com.heytap.cdo.splash.domain.dto.v4.SplashDtoV4"
            val mediaDto = "com.heytap.cdo.splash.domain.dto.v4.MediaComponentDtoV4"
            val imageDto = "com.heytap.cdo.splash.domain.dto.v4.ImageComponentDtoV4"

            //Source SplashTransaction
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(Int::class.java)
                        addForType(Long::class.java)
                        addForType(Boolean::class.java)
                        addForType(AtomicBoolean::class.java)
                    }
                    methods {
                        add { paramTypes(String::class.java); returnType(Boolean::class.java) }
                        add { paramTypes(Boolean::class.java); returnType(splashDto) }
                        add {
                            paramTypes(Boolean::class.java.name, Int::class.java.name, splashDto)
                            returnType(Void.TYPE)
                        }
                        add { paramTypes(splashDto, Boolean::class.java.name, mediaDto) }
                        add { paramTypes(splashDto, Boolean::class.java.name, imageDto) }
                    }
                    usingStrings("getSplashData")
                }
            }.apply {
                checkDataList("RemoveMarketSplashPageAppRecommend")
                single().name.toClass().resolve().apply {
                    firstMethod {
                        parameters(Boolean::class.java)
                        returnType(splashDto)
                    }.hook {
                        intercept()
                    }
                }
            }
        }
    }

    @Obfuscate
    class MarketSplashPageV2(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            val splashDto = "com.heytap.cdo.splash.domain.dto.v2.SplashDto"
            val mediaDto = "com.heytap.cdo.splash.domain.dto.v2.MediaComponentDto"
            val imageDto = "com.heytap.cdo.splash.domain.dto.v2.ImageComponentDto"

            //Source SplashTransaction
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(Int::class.java)
                        addForType(Long::class.java)
                        addForType(Boolean::class.java)
                        addForType(AtomicBoolean::class.java)
                    }
                    methods {
                        add { paramTypes(String::class.java); returnType(Boolean::class.java) }
                        add { paramTypes(Boolean::class.java); returnType(splashDto) }
                        add {
                            paramTypes(Boolean::class.java.name, Int::class.java.name, splashDto)
                            returnType(Void.TYPE)
                        }
                        add { paramTypes(splashDto, Boolean::class.java.name, mediaDto) }
                        add { paramTypes(splashDto, Boolean::class.java.name, imageDto) }
                    }
                    usingStrings("getSplashData")
                }
            }.apply {
                checkDataList("RemoveMarketSplashPageAppRecommend")
                single().name.toClass().resolve().apply {
                    firstMethod {
                        parameters(Boolean::class.java)
                        returnType(splashDto)
                    }.hook {
                        intercept()
                    }
                }
            }
        }
    }
}