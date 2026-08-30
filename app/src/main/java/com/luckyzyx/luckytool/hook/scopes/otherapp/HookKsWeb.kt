package com.luckyzyx.luckytool.hook.scopes.otherapp

import android.content.Context
import android.content.SharedPreferences
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookKsWeb : Hooker {
    override fun onHook() {
        val isPro = prefs(ModulePrefs).getBoolean("ksweb_remove_check_license", false)
        if (!isPro) return
        //Source EXTEND TO PRO VERSION / CHECK SERIAL KEY / KSWEB PRO / KSWEB STANDARD
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(Int::class.java)
                        addForType(Boolean::class.java)
                        addForType(SharedPreferences::class.java)
                    }
                    methods {
                        add { paramCount(0);returnType(Int::class.java) }
                        add { paramCount(0);returnType(Boolean::class.java) }
                        add { paramTypes(Int::class.java);returnType(Void.TYPE) }
                        add { paramTypes(Context::class.java);returnType(Void.TYPE) }
                    }
                    usingStrings(
                        "EXTEND TO PRO VERSION",
                        "CHECK SERIAL KEY",
                        "KSWEB PRO",
                        "KSWEB STANDARD"
                    )
                }
            }.apply {
                checkDataList("HookKsWeb")
                single().name.toClass().resolve().apply {
                    method {
                        emptyParameters()
                        returnType = Boolean::class
                    }.hookAll {
                        before {
                            firstField { type = Boolean::class }.of(instance).set(true)
                            firstField { type = Int::class }.of(instance).set(2)
                        }
                    }
                }
            }
        }
    }
}