package com.luckyzyx.luckytool.hook.scopes.market

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.isSubclassOf
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.XLog
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveMarketSearchPageAppRecommend(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        val cardDto = "com.heytap.cdo.card.domain.dto.CardDto"
        val viewLayerWrapDto = "com.heytap.cdo.card.domain.dto.ViewLayerWrapDto"
        val resourceDto = "com.heytap.cdo.common.domain.dto.ResourceDto"
        val appListCardDto = "com.heytap.cdo.card.domain.dto.AppListCardDto"

        val horizontalAppItemView = "com.nearme.cards.widget.view.HorizontalAppItemView"

        //Source IAppCard
        val cardMethod = dexKitBridge.findClass {
            matcher {
                className("com.nearme.cards.widget.card.Card")
            }
        }.let {
            it.checkDataList("RemoveMarketSearchPageAppRecommend CardCls")
            it.findMethod {
                matcher {
                    paramTypes(cardDto)
                    returnType(Void.TYPE)
                }
            }.checkDataList("RemoveMarketSearchPageAppRecommend CardMethod").single()
        }

        //Source HorizontalAppCard
        "com.nearme.cards.widget.card.impl.horizontalapp.HorizontalAppCard".toClass().resolve()
            .apply {
                firstMethod {
                    name { it != cardMethod.methodName }
                    parameters(cardDto)
                    returnType = Void.TYPE
                }.hook {
                    before {
                        args().first().setNull()
                    }
                }
                firstMethod {
                    name = cardMethod.methodName
                    parameters(cardDto)
                    returnType = Void.TYPE
                }.hook {
                    after {
                        val dto = args().first().any() ?: return@after
                        val viewGroup = firstField { type = horizontalAppItemView }.of(instance)
                            .get<ViewGroup>()

                        XLog.debug("$dto")

                        val code =
                            dto.asResolver().firstMethod { name = "getCode";superclass() }.invoke()
                        val key =
                            dto.asResolver().firstMethod { name = "getKey";superclass() }.invoke()
                        XLog.debug("code: $code | key: $key")

                        val parent = viewGroup?.parent
                        when (key) {
                            //搜索页
                            56432, 56433 -> {
                                if (parent is ViewGroup) {
                                    parent.isVisible = false
                                }
                            }

                            //详情页
                            57963, 54067 -> {
                                if (parent is ViewGroup) {
                                    parent.isVisible = false
                                }
                            }

                        }
                    }
                }
            }

        //热门App合集
        //Source SearchHotInstallRecycleCard
        "com.nearme.cards.widget.card.impl.search.SearchHotInstallRecycleCard".toClass().resolve()
            .apply {
                firstMethod {
                    parameters(cardDto)
                    returnType = Void.TYPE
                }.hook {
                    before {
                        val dto = args().first().any() ?: return@before
                        if (dto::class isSubclassOf appListCardDto.toClass()) {
                            args().first().setNull()
                        }
                    }
                }
            }
    }
}