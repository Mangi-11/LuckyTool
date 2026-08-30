package com.luckyzyx.luckytool.hook.globals

import android.util.ArrayMap
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.hook.core.result
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookGlobalPmsFeature : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode
        val list = ArrayMap<String, Boolean>().apply {

        }
        loadHooker(PmsFeature(list))
    }

    @Obfuscate
    class PmsFeature(private val features: Map<String, Boolean>) : Hooker {
        override fun onHook() {
            //Source PackageManagerService
            "com.android.server.pm.PackageManagerService".toClass().resolve().apply {
                firstMethod {
                    name = "hasSystemFeature"
                    parameters(String::class, Int::class)
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
        }
    }
}