package com.luckyzyx.luckytool.hook

import org.lsposed.lsparanoid.Obfuscate

/**
 * libxposed 路由注册表：对应迁移前 YukiEntry 的 loadApp 清单。
 * 按波次逐步从 YukiEntry 平移至此；未迁移的包仍走 legacy 入口。
 */
@Obfuscate
object HookRouterInit {

    fun register() {
        //后续波次按 YukiEntry 清单平移，例：
        //HookRouter.app("com.android.systemui", HookSystemUI)
        //HookRouter.app("com.oplus.camera", HookCamera)
        //HookRouter.system(HookAndroid)
    }
}