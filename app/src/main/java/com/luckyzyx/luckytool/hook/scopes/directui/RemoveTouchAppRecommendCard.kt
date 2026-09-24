package com.luckyzyx.luckytool.hook.scopes.directui

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveTouchAppRecommendCard : YukiBaseHooker() {
    override fun onHook() {
        //Source DirectUIMainViewMode -> AppBean
        "com.coloros.directui.repository.datasource.AppBean".toClass().resolve().apply {
            firstMethod { name = "toCardUIInfo" }.hook {
                intercept()
            }
        }
    }
}