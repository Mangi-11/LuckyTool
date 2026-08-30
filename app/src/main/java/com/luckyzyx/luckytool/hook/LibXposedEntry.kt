package com.luckyzyx.luckytool.hook

import com.luckyzyx.luckytool.hook.core.Env
import com.luckyzyx.luckytool.hook.core.HookRouter
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import org.lsposed.lsparanoid.Obfuscate

/**
 * libxposed (Modern Xposed API) 模块入口。
 *
 * 由 META-INF/xposed/java_init.list 注册（R8 会同步改写混淆后的类名）。
 * 装载顺序：onModuleLoaded（注入框架接口 + 注册路由表）→
 * 每个 scoped 包就绪时 onPackageReady → 分发到 HookRouter；
 * system_server 由 onSystemServerStarting 覆盖首个包回调（packageName = "android"）。
 */
@Obfuscate
class LibXposedEntry : XposedModule() {

    override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
        Env.attach(this, param.processName)
        HookRouterInit.register()
        Env.log(android.util.Log.INFO, "LuckyTool", "LibXposedEntry loaded in ${param.processName}")
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        HookRouter.dispatch(param.packageName, param.classLoader, param.applicationInfo)
    }

    override fun onSystemServerStarting(param: XposedModuleInterface.SystemServerStartingParam) {
        HookRouter.dispatch("android", param.classLoader, null)
    }
}