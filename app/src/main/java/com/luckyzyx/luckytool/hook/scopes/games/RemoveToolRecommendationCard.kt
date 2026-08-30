package com.luckyzyx.luckytool.hook.scopes.games

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveToolRecommendationCard : Hooker {
    override fun onHook() {
        //Source ToolsRecommendCardLayout
        "business.module.toolsrecommend.ToolsRecommendCardLayout".toClassOrNull()?.resolve()
            ?.apply {
                firstMethod {
                    parameters(List::class)
                    returnType = Void.TYPE
                }.hook {
                    before {
                        args().first().set(ArrayList<Any>())
                    }
                }
            }

        //Source GameToolTileAdapter V9.0.0+
//        "business.toolpanel.adapter.GameToolTileAdapter".toClassOrNull()?.apply {
//            method { name = "onCreateViewHolder" }.hook {
//                after {
//                    val parent = args().first().cast<ViewGroup>() ?: return@after
//                    val id = args().last().int()
//                    if (id == 10005) result<ViewHolder>()?.itemView?.isVisible = false
//                }
//            }
//        }
    }
}