package com.luckyzyx.luckytool.hook.scopes.calendar

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.result
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveHolidayPageInformationFlow : Hooker {
    override fun onHook() {
        //Source SpecialHolidayWebViewDetailViewModel > V13.9.16
        "com.coloros.calendar.app.specialholiday.SpecialHolidayWebViewDetailViewModel".toClassOrNull()
            ?.resolve()?.apply {
                firstMethod { name = "buildHolidayH5UrlInner" }.hook {
                    after {
                        val res = result<String>() ?: return@after
                        result = formatCalendarUrl(res)
                    }
                }
            }
    }

    private fun formatCalendarUrl(url: String): String {
        if (url.isBlank()) return url
        var cacheUrl = url
        if (cacheUrl.contains("&showAd=1")) cacheUrl = cacheUrl.replace(
            "&showAd=1", ""
        )
        return cacheUrl
    }
}