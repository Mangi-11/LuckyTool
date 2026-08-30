package com.luckyzyx.luckytool.hook.scopes.quicksearchbox

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.kavaref.extension.classOf
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveSearchBoxAppRecommendCard : Hooker {
    override fun onHook() {
        //Source AliveAppRecommendView -> view_alive_app
        VariousClass(
            "com.heytap.quicksearchbox.ui.widget.AliveAppRecommendView",
            "com.heytap.quicksearchbox.ui.widget.advicesub.AliveAppRecommendView" //C15
        ).toClass().resolve().apply {
            firstMethod {
                parameters { it[0] == classOf<List<*>>() && it[1] == classOf<Boolean>() }
                parameterCount { it in 2..4 }
            }.hook {
                before {
                    args().first().cast<java.util.ArrayList<Any>>()?.clear()
                }
            }
        }
    }
}