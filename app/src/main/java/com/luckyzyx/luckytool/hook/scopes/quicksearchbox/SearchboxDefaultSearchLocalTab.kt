package com.luckyzyx.luckytool.hook.scopes.quicksearchbox

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class SearchboxDefaultSearchLocalTab(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source SearchResultFragment -> getDefaultTabId
        //Search From com.heytap.common.constants.Tab -> GENERAL -> LOCAL
        dexKitBridge.findClass {
            matcher {
                className("com.heytap.quicksearchbox.ui.fragment.SearchResultFragment")
            }
        }.apply {
            checkDataList("SearchResultFragment")

            findMethod {
                matcher {
                    paramCount(0)
                    returnType(String::class.java)
                    addUsingField {
                        type("com.heytap.quicksearchbox.core.localsearch.SearchParams")
                    }
                    addUsingField {
                        type("com.heytap.common.bean.TabItems")
                    }
                    addCaller {
                        paramCount(0)
                        returnType(Void.TYPE)
                    }
                }
            }.apply {
                checkDataList("getDefaultTabId")

                single().className.toClass().resolve().apply {
                    firstMethod {
                        name = single().methodName
                        emptyParameters()
                        returnType = String::class
                    }.hook {
                        replaceTo("local")
                    }
                }
            }
        }
    }
}