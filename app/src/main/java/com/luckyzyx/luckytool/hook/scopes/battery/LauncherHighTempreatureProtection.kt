package com.luckyzyx.luckytool.hook.scopes.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.PowerManager
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class LauncherHighTempreatureProtection(val dexKitBridge: DexKitBridge) : Hooker {
    val key = "LauncherHighTempreatureProtection"
    override fun onHook() {
        //Source ThermalHandler high_temperature_shutdown_message / high_temperature_dialog_auto
        //Key oplus_settings_hightemp_protect 1004
        dexKitBridge.findClass {
            matcher {
                fields {
                    addForType(Int::class.java)
                    addForType(Context::class.java)
                    addForType(Handler::class.java)
                    addForType(PowerManager::class.java)
                    addForType(SharedPreferences::class.java)
                    addForType(BroadcastReceiver::class.java)
                }
                methods {
                    add { name("handleMessage") }
                    add { paramTypes(Context::class.java) }
                    add { paramTypes(Int::class.java, Int::class.java) }
                }
            }
        }.apply {
            checkDataList("LauncherHighTempreatureProtection")
            single().name.toClass().resolve().apply {
                firstConstructor { parameterCount = 3 }.hook {
                    intercept()
                }
            }
        }
    }
}