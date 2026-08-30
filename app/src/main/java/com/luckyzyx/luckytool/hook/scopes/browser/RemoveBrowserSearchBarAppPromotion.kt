package com.luckyzyx.luckytool.hook.scopes.browser

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveBrowserSearchBarAppPromotion(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        val appHostCls = "com.heytap.browser.platform.app.AppHost"
        if (appHostCls.toClassOrNull() == null) return

        //Source MultiSugItemData
        val app = dexKitBridge.findClass {
            matcher {
                addFieldForType(String::class.java)
                addMethod { paramCount(0);returnType(String::class.java) }
                usingStrings("res", "initialState", "sugNaturalApp")
            }
        }.checkDataList("RemoveBrowserSearchBarAppPromotion App")

        //Source MultiSugItemData
        val ads = dexKitBridge.findClass {
            matcher {
                addFieldForType(Int::class.java)
                addFieldForType(String::class.java)
                addMethod { name("getTitle") }
                addMethod { name("getCategoryType") }
                addMethod { paramCount(0);returnType(Int::class.java) }
                addMethod { paramCount(0);returnType(String::class.java) }
                usingStrings("res", "ad", "sugAd")
            }
        }.checkDataList("RemoveBrowserSearchBarAppPromotion Ads")

        //Source SugMultiAdapter
        dexKitBridge.findClass {
            matcher {
                fields {
                    addForType(List::class.java)
                    addForType(ArrayList::class.java)
                    addForType(Map::class.java)
                    addForType(Int::class.java)
                    addForType(appHostCls)
                }
                methods {
                    add { paramCount(0);returnType(List::class.java) }
                    add {
                        paramTypes(
                            Int::class.java,
                            Int::class.java,
                            Int::class.java,
                            Int::class.java
                        )
                    }
                    add { paramTypes(List::class.java);returnType(Void.TYPE) }
                    add { paramTypes(appHostCls);returnType(Void.TYPE) }
                    add { name("getItemCount") }
                    add { name("getItemViewType") }
                    add { name("onAttachedToRecyclerView") }
                    add { name("onBindViewHolder") }
                    add { name("onCreateViewHolder") }
                    add { name("onViewAttachedToWindow") }
                }
            }
        }.apply {
            checkDataList("RemoveBrowserSearchBarAppPromotion Adapter")
            findMethod {
                matcher {
                    paramTypes(List::class.java)
                    returnType(Void.TYPE)
                    usingStrings("linkEdit")
                    addCaller {
                        paramTypes(List::class.java)
                        returnType(Void.TYPE)
                        usingStrings("headerData", "linkEdit")
                    }
                }
            }.apply {
                checkDataList("RemoveBrowserSearchBarAppPromotion Method")
                single().className.toClass().resolve().apply {
                    firstMethod {
                        name = single().methodName
                        parameters(List::class)
                    }.hook {
                        before {
                            val list = args().first().cast<ArrayList<Any>>() ?: return@before
                            list.removeIf {
//                                YLog.debug("${list.indexOf(it)} -> $it")
                                it.javaClass.name == app.singleOrNull()?.name || it.javaClass.name == ads.singleOrNull()?.name
                            }
                        }
                    }
                }
            }
        }
    }
}