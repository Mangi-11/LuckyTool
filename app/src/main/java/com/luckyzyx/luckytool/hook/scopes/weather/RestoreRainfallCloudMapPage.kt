package com.luckyzyx.luckytool.hook.scopes.weather

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RestoreRainfallCloudMapPage : Hooker {
    override fun onHook() {
        //Source IndexOperationsManager
        "com.oplus.weather.indexoperations.IndexOperationsManager".toClassOrNull()?.resolve()
            ?.apply {
                firstMethod { name = "supportIndexOperationsFeature" }.hook {
                    replaceToFalse()
                }
            }
    }
}