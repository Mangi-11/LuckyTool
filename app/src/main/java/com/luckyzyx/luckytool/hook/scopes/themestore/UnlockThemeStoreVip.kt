package com.luckyzyx.luckytool.hook.scopes.themestore

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object UnlockThemeStoreVip : Hooker {
    override fun onHook() {
        //Source VipUserDto
        "com.oppo.cdo.card.theme.dto.vip.VipUserDto".toClassOrNull()?.resolve()?.apply {
            firstMethod { name = "getVipStatus" }.hook {
                replaceTo(1)
            }
            firstMethod { name = "getVipDays" }.hook {
                replaceTo(999)
            }
        }

        //Source WeatherPageResponseDto
        "com.oppo.cdo.card.theme.dto.page.WeatherPageResponseDto".toClassOrNull()?.resolve()
            ?.apply {
                firstMethod { name = "getVipStatus" }.hook {
                    replaceTo(1)
                }
            }

        //Source ResourceItemDto
        "com.oppo.cdo.theme.domain.dto.response.ResourceItemDto".toClassOrNull()?.resolve()?.apply {
            firstMethod { name = "getIsVip" }.hook {
                replaceTo(1)
            }
            firstMethod { name = "getIsVipAvailable" }.hook {
                replaceTo(1)
            }
        }

        //Source PublishProductItemDto
        "com.oppo.cdo.theme.domain.dto.response.PublishProductItemDto".toClassOrNull()?.resolve()
            ?.apply {
                firstMethod { name = "getPrice" }.hook {
                    replaceTo(0.0)
                }
                firstMethod { name = "getIsVipAvailable" }.hook {
                    replaceTo(1)
                }
            }

        //Source SplashDto
        "com.oppo.cdo.card.theme.dto.SplashDto".toClassOrNull()?.resolve()?.apply {
            firstMethod { name = "getAdData" }.hook {
                replaceTo(null)
            }
            firstMethod { name = "getShowTime" }.hook {
                replaceTo(1)
            }
            firstMethod { name = "getIsSkip" }.hook {
                replaceToTrue()
            }
        }

        //Source ThemeTrialExpireReceiver
        "com.nearme.themespace.trial.ThemeTrialExpireReceiver".toClassOrNull()?.resolve()?.apply {
            firstMethod { name = "onReceive" }.hook {
                intercept()
            }
        }
    }
}