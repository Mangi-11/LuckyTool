package com.luckyzyx.luckytool.hook.scopes.gallery

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class HookFunctionManager(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //姜文电影滤镜
        val jangWen = prefs(ModulePrefs).getBoolean("enable_gallery_jiangwen_filter", false)

        //Source FunctionSwitchManager
        dexKitBridge.findClass {
            matcher {
                fields {
                    addForType(Map::class.java)
                }
                methods {
                    add {
                        paramTypes(String::class.java)
                        returnType(Boolean::class.java)
                        usingStrings("FunctionSwitchManager", "getGroupName", "spKey")
                    }
                    add {
                        paramCount(1..5)
                        returnType(Void.TYPE)
                    }
                }
                usingStrings("FunctionSwitchManager")
            }
        }.apply {
            checkDataList("HookFunctionManager")
            single().name.toClass().resolve().apply {
                firstMethod {
                    parameters(String::class)
                    returnType(Boolean::class)
                }.hook {
                    after {
                        when (args().first().string()) {
                            //姜文电影滤镜
                            "pref_jiangwen_filter_enable" -> if (jangWen) resultTrue()
                        }
                    }
                }
            }
        }
    }
}