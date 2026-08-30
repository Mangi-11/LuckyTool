package com.luckyzyx.luckytool.hook

import com.luckyzyx.luckytool.hook.core.HookRouter
import com.luckyzyx.luckytool.hook.scopes.battery.UnlockStartupLimit
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.hook.core.Hooker

/**
 * libxposed 路由注册表：对应迁移前 YukiEntry 的 loadApp 清单。
 * 按波次逐步从 YukiEntry 平移至此；未迁移的包仍走 legacy 入口。
 */
@Obfuscate
object HookRouterInit {

    fun register() {
        //W1 试点：电池自启数量限制
        HookRouter.app("com.oplus.battery", BatteryPilotHooker)

        //后续波次按 YukiEntry 清单平移，例：
        //HookRouter.app("com.android.systemui", HookSystemUI)
        //HookRouter.app("com.oplus.camera", HookCamera)
        //HookRouter.system(HookAndroid)
    }

    /** 试点 Hooker：复刻 HookBattery 的 DexKit 装载流程（仅迁移自启限制一项） */
    private object BatteryPilotHooker : Hooker {
        override fun onHook() {
            val sourceDir = appInfo?.sourceDir ?: return
            if (SDK < A13) try {
                DexkitUtils.create(sourceDir).close()
            } catch (_: UnsatisfiedLinkError) {
                return
            }
            DexkitUtils.create(sourceDir) { bridge ->
                if (prefs(ModulePrefs).getBoolean("unlock_startup_limit", false)) {
                    if (SDK >= A13) loadHooker(UnlockStartupLimit(bridge))
                }
            }
        }
    }
}