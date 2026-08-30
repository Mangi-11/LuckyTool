package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.directui.RemoveTouchAppRecommendCard
import com.luckyzyx.luckytool.utils.ModulePrefs

@Obfuscate
object HookDirectUI : Hooker {
    override fun onHook() {
        //移除应用推广卡片
        if (prefs(ModulePrefs).getBoolean("remove_touch_app_recommend_card", false)) {
            loadHooker(RemoveTouchAppRecommendCard)
        }
    }
}