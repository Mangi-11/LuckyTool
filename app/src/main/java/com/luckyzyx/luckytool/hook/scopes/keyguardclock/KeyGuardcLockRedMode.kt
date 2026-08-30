package com.luckyzyx.luckytool.hook.scopes.keyguardclock

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class KeyGuardcLockRedMode(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        val redMode = prefs(ModulePrefs).getString("lock_screen_clock_redone_mode", "0")

        //Source CustomizedTextView -> BrandUtils
        dexKitBridge.findClass {
            matcher {
                addFieldForType(Boolean::class.java)
                usingStrings("ro.oplus.image.system_ext.brand", "ro.oplus.image.system_ext.area")
            }
        }.apply {
            checkDataList("KeyGuardcLockRedMode Clazz")
            findField {
                matcher {
                    type(Boolean::class.java)
                    addReadMethod {
                        paramCount(1)
                        returnType(Void.TYPE)
                        usingStrings("1")
                        usingNumbers(1)
                    }
                }
            }.apply {
                checkDataList("KeyGuardcLockRedMode Field")
                single().className.toClass(initialize = true).resolve().apply {
                    firstField {
                        name = single().fieldName
                        type = Boolean::class
                    }.set(
                        when (redMode) {
                            "1" -> true
                            "2" -> false
                            else -> return
                        }
                    )
                }
            }
        }
    }
}
