package com.luckyzyx.luckytool.hook.scopes.market

import android.content.Intent
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.XLog
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.result
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class RemoveMarketDetailPageAppRecommend(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //com.heytap.cdo.client.detail.app.AppDetailActivity
        //com.heytap.cdo.client.detail.app.base.ScrollContentView

        "com.heytap.cdo.client.detail.app.AppDetailActivity".toClass().resolve().apply {
            firstMethod { parameters(Intent::class) }.hook {
                after {
                    val resourceDto = result<Any>()
                    if (resourceDto == null) {
                        XLog.debug("resourceDto is null")
                        return@after
                    }

                    "com.heytap.cdo.common.domain.dto.ResourceDto".toClass().fields
                        .forEachIndexed { index, field ->
                            val ff = field.get(resourceDto)
                            XLog.debug("$index | ${field.name} -> $ff")
                        }

                }
            }
        }
    }
}