package com.luckyzyx.luckytool.hook.hookers

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.hook.scopes.battery.DisplayModuleCalculatesBatteryHealthData
import com.luckyzyx.luckytool.hook.scopes.battery.HookBatteryNotify
import com.luckyzyx.luckytool.hook.scopes.battery.LauncherHighTempreatureProtection
import com.luckyzyx.luckytool.hook.scopes.battery.RemoveBatteryRestrictPlugin
import com.luckyzyx.luckytool.hook.scopes.battery.RemoveBatteryTemperatureControl
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookBattery : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A13) try {
            DexkitUtils.create(appInfo.sourceDir).close()
        } catch (_: UnsatisfiedLinkError) {
            return
        }

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            //电池通知
            loadHooker(HookBatteryNotify(dexKitBridge))
            //移除自启数量限制（已迁移至 libxposed 路由 HookRouterInit -> UnlockStartupLimit）
            //移除电池温度控制
            if (prefs(ModulePrefs).getBoolean("remove_battery_temperature_control", false)) {
                loadHooker(RemoveBatteryTemperatureControl(dexKitBridge))
                loadHooker(LauncherHighTempreatureProtection(dexKitBridge))
            }
            //移除电池限制插件
            if (prefs(ModulePrefs).getBoolean("remove_battery_restrict_plugin", false)) {
                loadHooker(RemoveBatteryRestrictPlugin(dexKitBridge))
            }
        }

        //显示模块计算电池健康数据
        if (prefs(ModulePrefs).getBoolean("open_battery_health", false)) {
            if (SDK >= A13) loadHooker(DisplayModuleCalculatesBatteryHealthData)
        }
    }
}