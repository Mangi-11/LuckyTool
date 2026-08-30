package com.luckyzyx.luckytool.hook.scopes.games

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class EnableSupportCompetitionMode(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source CompetitionModeManager
        //Search isSupportCompetitionMode
        dexKitBridge.findClass {
            matcher {
                fields {
                    addForType(List::class.java)
                }
                methods {
                    add { paramCount(0);returnType(List::class.java) }
                    add { paramCount(0);returnType(Boolean::class.java) }
                    add { paramTypes(String::class.java, ArrayList::class.java) }
                }
            }
        }.apply {
            checkDataList("EnableSupportCompetitionMode find CompetitionModeManager")

            findMethod {
                matcher {
                    paramCount(0)
                    returnType(Boolean::class.java)
                    usingStrings("isSupportCompetitionMode")
                }
            }.apply {
                checkDataList("EnableSupportCompetitionMode find isSupportCompetitionMode")

                single().className.toClass().resolve().apply {
                    firstMethod {
                        name = single().methodName
                        emptyParameters()
                        returnType = Boolean::class
                    }.hook {
                        replaceToTrue()
                    }
                }
            }
        }
    }
}