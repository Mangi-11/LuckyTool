package com.luckyzyx.luckytool.hook.scopes.games

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveStartupAnimation(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source GameOptimizedNewView
        //Search -> startAnimationIn -> Method
        dexKitBridge.findClass {
            matcher {
                className("business.secondarypanel.view.GameOptimizedNewView")
            }
        }.apply {
            checkDataList("RemoveStartupAnimation find GameOptimizedNewView")

            findMethod {
                matcher {
                    paramCount(0)
                    usingStrings("startAnimationIn")
                }
            }.apply {
                checkDataList("RemoveStartupAnimation find startAnimationIn")

                single().className.toClass().resolve().apply {
                    firstMethod { name = single().methodName }.hook {
                        intercept()
                    }
                }
            }
        }
    }
}
