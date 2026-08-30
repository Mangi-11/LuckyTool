package com.luckyzyx.luckytool.hook.scopes.launcher

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.result
import com.luckyzyx.luckytool.hook.utils.launcher.LauncherAppStateUtils
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveDockerMaxNumberLimit : Hooker {
    override fun onHook() {
        //Source ExpandConfig
        "com.android.launcher3.hotseat.expand.ExpandConfig".toClass().resolve().apply {
            firstMethod {
                name = "getHotseatNormalItemsMaxCountBy"
                parameters(Boolean::class, Boolean::class)
                returnType = Int::class
            }.hook {
                after {
                    LauncherAppStateUtils(appClassLoader).apply {
                        val state = getInstanceNoCreate() ?: return@after
                        val idp = getInvariantDeviceProfile(state) ?: return@after
                        val col = idp.asResolver().firstMethod {
                            name = "getNumColumns"; superclass()
                        }.invoke<Int>() ?: return@after
                        val res = result<Int>() ?: return@after
                        if (col > res) result = col
                    }
                }
            }
        }
    }
}