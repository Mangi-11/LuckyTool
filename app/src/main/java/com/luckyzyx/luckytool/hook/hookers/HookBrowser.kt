package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.browser.RemoveAdsAtDownloadPageBottom
import com.luckyzyx.luckytool.hook.scopes.browser.RemoveAdsFromDownloadDialog
import com.luckyzyx.luckytool.hook.scopes.browser.RemoveBrowserSearchBarAppPromotion
import com.luckyzyx.luckytool.hook.scopes.browser.RemoveBrowserWindowLimitNumber
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs

@Obfuscate
object HookBrowser : Hooker {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            //移除下载对话框广告
            if (prefs(ModulePrefs).getBoolean("remove_ads_from_download_dialog", false)) {
                loadHooker(RemoveAdsFromDownloadDialog(dexKitBridge))
            }
            //移除下载页面底部广告
            if (prefs(ModulePrefs).getBoolean("remove_ads_at_download_page_bottom", false)) {
                loadHooker(RemoveAdsAtDownloadPageBottom(dexKitBridge))
            }
            //移除浏览器窗口数量限制
            if (prefs(ModulePrefs).getBoolean("remove_browser_window_limit_number", false)) {
                loadHooker(RemoveBrowserWindowLimitNumber(dexKitBridge))
            }
            //移除浏览器搜索框App推广
            if (prefs(ModulePrefs).getBoolean("remove_browser_search_bar_app_promotion", false)) {
                loadHooker(RemoveBrowserSearchBarAppPromotion(dexKitBridge))
            }
        }
//        //移除天气页面广告
//        if (prefs(ModulePrefs).getBoolean("remove_ads_from_weather_page", false)) {
//            loadHooker(RemoveAdsFromWeatherPage)
//        }
    }
}