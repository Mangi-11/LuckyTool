package com.luckyzyx.luckytool.hook.scopes.games

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveSomeVipLimit : Hooker {
    override fun onHook() {
        //network_speed_vip -> oppo_acc

        //Source VipInfoBean -> VipInfosDTO
        //<string name="magic_voice_buy_vip_tip">开启游戏变声，尽享全部变声效果</string>
        "com.oplus.games.account.bean.VipInfoBean\$VipInfosDTO".toClass().resolve().apply {
            firstMethod { name = "getVip" }.hook {
                replaceToTrue()
            }
            firstMethod { name = "getExpiredVip" }.hook {
                replaceToFalse()
            }
            firstMethod { name = "getExpireTime" }.hook {
                replaceTo("2999-12-31")
            }
            firstMethod { name = "getSign" }.hook {
                replaceToTrue()
            }
        }
        //Source VipAccelearateResponse
        "com.oplus.games.account.bean.VipAccelearateResponse".toClass().resolve().apply {
            firstMethod { name = "getSuperBooster" }.hook {
                replaceToTrue()
            }
            firstMethod { name = "isSuperBooster" }.hook {
                replaceToTrue()
            }
        }
        //Source VIPStateBean
        "com.oplus.games.account.bean.VIPStateBean".toClass().resolve().apply {
            firstMethod { name = "getVipState" }.hook {
                replaceTo(5)
            }
            firstMethod { name = "getExpireTime" }.hook {
                replaceTo("2999-12-31")
            }
        }
        //Source UserInfo
        "com.coloros.gamespaceui.module.magicvoice.oplus.data.UserInfo".toClass().resolve().apply {
            firstMethod { name = "getExpireTime" }.hook {
                replaceTo("2999-12-31")
            }
            firstMethod { name = "getHasTrialQualifications" }.hook {
                replaceToTrue()
            }
            firstMethod { name = "getUserIdentity" }.hook {
                replaceTo(3)
            }
        }
    }
}