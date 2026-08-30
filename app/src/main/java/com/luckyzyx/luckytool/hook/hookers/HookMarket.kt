package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.market.RemoveMarketMinePageAppRecommend
import com.luckyzyx.luckytool.hook.scopes.market.RemoveMarketSplashPageAppRecommend
import com.luckyzyx.luckytool.hook.scopes.market.RemoveMarketUpdateDownloadPageAppRecommend
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs

@Obfuscate
object HookMarket : Hooker {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            //移除商店启动页应用推荐
            if (prefs(ModulePrefs).getBoolean("remove_market_splash_page_app_recommend", false)) {
                loadHooker(RemoveMarketSplashPageAppRecommend(dexKitBridge))
            }
            //移除商店更新下载页面应用推荐
            if (prefs(ModulePrefs).getBoolean("remove_market_update_download_page_app_recommend", false)) {
                loadHooker(RemoveMarketUpdateDownloadPageAppRecommend(dexKitBridge))
            }
            //移除软件商店我的页面应用推荐
            if (prefs(ModulePrefs).getBoolean("remove_market_mine_page_app_recommend", false)) {
                loadHooker(RemoveMarketMinePageAppRecommend(dexKitBridge))
            }
        }
    }
}