package com.luckyzyx.luckytool.hook.scopes.directui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveTouchAppRecommendCard : Hooker {
    override fun onHook() {
        //Source DirectUIMainViewMode -> AppBean
        "com.coloros.directui.repository.datasource.AppBean".toClass().resolve().apply {
            firstMethod { name = "toCardUIInfo" }.hook {
                intercept()
            }
        }
    }
}