package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.oplus.media.OplusMediaControlManager
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object DisableMediaMusicFluidCloudBlacklist : Hooker {
    override fun onHook() {
        //Source OplusMediaDataModelImpl
        "com.oplus.systemui.media.model.OplusMediaDataModelImpl".toClass().resolve().apply {
            firstMethod {
                name = "setMediaControlBlackList"
                parameters(List::class)
            }.hook {
                before {
                    val manager = firstField {
                        type = OplusMediaControlManager::class
                    }.of(instance).get<OplusMediaControlManager>() ?: return@before
                    manager.setMediaControlDenyList(listOf(""))
                    resultNull()
                }
            }
        }
    }
}