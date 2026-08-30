package com.luckyzyx.luckytool.hook.scopes.settings

import android.content.pm.ApplicationInfo
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

class HookSettingsFeature(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        if (SDK < A13) loadHooker(HookExpUst(dexKitBridge))
    }

    @Obfuscate
    class HookExpUst(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            val neverTimeout = prefs(ModulePrefs).getBoolean("enable_show_never_timeout", false)

            //Source ExpUstUtils
            dexKitBridge.findClass {
                matcher {
                    methods {
                        add { returnType(String::class.java) }
                        add { returnType(Boolean::class.java) }
                        add { returnType(ApplicationInfo::class.java) }
                        add { paramTypes(String::class.java) }
                        add { paramTypes(Int::class.java) }
                        add { paramTypes(Int::class.java, String::class.java) }
                        add { paramTypes(String::class.java) }
                        add { paramTypes(String::class.java, String::class.java) }
                    }
                    usingStrings("screen_off_timeout")
                }
            }.apply {
                checkDataList("HookExpUst")
                single().name.toClass().resolve().apply {
                    method {
                        parameters(Int::class)
                        returnType = Boolean::class
                    }.hookAll {
                        before {
                            when (args().first().int()) {
                                //Source DisplayTimeOutController -> 永不息屏(24H)
                                11 -> if (SDK < A13 && neverTimeout) resultTrue()
                            }
                        }
                    }
                }
            }
        }
    }
}