package com.luckyzyx.luckytool.hook

import android.annotation.SuppressLint
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.luckyzyx.luckytool.hook.hookers.HookAlarmClock
import com.luckyzyx.luckytool.hook.hookers.HookAndroid
import com.luckyzyx.luckytool.hook.hookers.HookAudioEffectCenter
import com.luckyzyx.luckytool.hook.hookers.HookAudioMonitor
import com.luckyzyx.luckytool.hook.hookers.HookBattery
import com.luckyzyx.luckytool.hook.hookers.HookBeaconLink
import com.luckyzyx.luckytool.hook.hookers.HookBrowser
import com.luckyzyx.luckytool.hook.hookers.HookCalendar
import com.luckyzyx.luckytool.hook.hookers.HookCamera
import com.luckyzyx.luckytool.hook.hookers.HookCloudService
import com.luckyzyx.luckytool.hook.hookers.HookDirectUI
import com.luckyzyx.luckytool.hook.hookers.HookEngineerMode
import com.luckyzyx.luckytool.hook.hookers.HookExternalStorage
import com.luckyzyx.luckytool.hook.hookers.HookFileManager
import com.luckyzyx.luckytool.hook.hookers.HookGallery
import com.luckyzyx.luckytool.hook.hookers.HookGesture
import com.luckyzyx.luckytool.hook.hookers.HookHealth
import com.luckyzyx.luckytool.hook.hookers.HookKeyguardClock
import com.luckyzyx.luckytool.hook.hookers.HookLauncher
import com.luckyzyx.luckytool.hook.hookers.HookMarket
import com.luckyzyx.luckytool.hook.hookers.HookMediaController
import com.luckyzyx.luckytool.hook.hookers.HookMultiApp
import com.luckyzyx.luckytool.hook.hookers.HookNfc
import com.luckyzyx.luckytool.hook.hookers.HookOShare
import com.luckyzyx.luckytool.hook.hookers.HookOplusCosa
import com.luckyzyx.luckytool.hook.hookers.HookOplusGames
import com.luckyzyx.luckytool.hook.hookers.HookOplusMMS
import com.luckyzyx.luckytool.hook.hookers.HookOplusOta
import com.luckyzyx.luckytool.hook.hookers.HookPackageInstaller
import com.luckyzyx.luckytool.hook.hookers.HookPermissionController
import com.luckyzyx.luckytool.hook.hookers.HookPhone
import com.luckyzyx.luckytool.hook.hookers.HookPhoneManager
import com.luckyzyx.luckytool.hook.hookers.HookPictorial
import com.luckyzyx.luckytool.hook.hookers.HookQuickSearchBox
import com.luckyzyx.luckytool.hook.hookers.HookSafeCenter
import com.luckyzyx.luckytool.hook.hookers.HookScreenshot
import com.luckyzyx.luckytool.hook.hookers.HookSecurePay
import com.luckyzyx.luckytool.hook.hookers.HookSecuritypPermission
import com.luckyzyx.luckytool.hook.hookers.HookSettings
import com.luckyzyx.luckytool.hook.hookers.HookSmartSidebar
import com.luckyzyx.luckytool.hook.hookers.HookSoundRecorder
import com.luckyzyx.luckytool.hook.hookers.HookSpeechAssist
import com.luckyzyx.luckytool.hook.hookers.HookSystemUI
import com.luckyzyx.luckytool.hook.hookers.HookThemeStore
import com.luckyzyx.luckytool.hook.hookers.HookUIEngine
import com.luckyzyx.luckytool.hook.hookers.HookWeather
import com.luckyzyx.luckytool.hook.hookers.HookWirelessSettings
import com.luckyzyx.luckytool.hook.scopes.otherapp.HookADM
import com.luckyzyx.luckytool.hook.scopes.otherapp.HookAlphaBackupPro
import com.luckyzyx.luckytool.hook.scopes.otherapp.HookFakeGpsJoyStick
import com.luckyzyx.luckytool.hook.scopes.otherapp.HookKsWeb
import com.luckyzyx.luckytool.utils.SettingsPrefs
import org.lsposed.lsparanoid.Obfuscate
import java.io.File

@Obfuscate
object YukiEntry {

    val configs = YukiHookAPI.configs {
        debugLog {
            tag = "LuckyTool"
            isEnable = true
            isRecord = true
            elements(TAG, PRIORITY, PACKAGE_NAME, USER_ID)
        }
        isDebug = false
    }

    fun PackageParam.onHookEntry() {
        if (prefs(SettingsPrefs).getBoolean("is_su", false).not()) return

        try {
            @SuppressLint("SdCardPath")
            val file = File("/sdcard/disable_lt")
            if (file.exists()) return
        } catch (_: Throwable) {

        }

        //系统框架
        loadSystem(HookAndroid)

        //系统界面
        loadApp("com.android.systemui", HookSystemUI)

        //经典主题 Clock
        loadApp("com.oplus.keyguard.clock.base", HookKeyguardClock)

        //通知管理
        //通知管理（已迁移至 libxposed 路由）

        //时钟
        loadApp("com.coloros.alarmclock", HookAlarmClock)
        //桌面
        loadApp("com.oppo.launcher", "com.android.launcher") {
            loadHooker(HookLauncher)
        }

        //息屏
        //百变引擎
        loadApp("com.oplus.uiengine", HookUIEngine)
        //截屏
        loadApp("com.oplus.screenshot", HookScreenshot)

        //安全中心
        loadApp("com.oplus.safecenter", "com.coloros.safecenter") {
            loadHooker(HookSafeCenter)
        }
        //应用安装器
        loadApp("com.android.packageinstaller", HookPackageInstaller)

        //外部存储设备
        loadApp("com.android.externalstorage", HookExternalStorage)

        //电池
        loadApp("com.oplus.battery", HookBattery)
        //设置
        loadApp("com.android.settings", HookSettings)
        //相机
        loadApp("com.oplus.camera", "com.oneplus.camera") {
            loadHooker(HookCamera)
        }
        //相册
        loadApp("com.coloros.gallery3d", HookGallery)
        //主题商店
        loadApp("com.heytap.themestore", "com.oplus.themestore") {
            loadHooker(HookThemeStore)
        }
        //云服务
        loadApp("com.heytap.cloud", HookCloudService)
        //游戏助手
        loadApp("com.oplus.games", HookOplusGames)
        //应用增强服务
        loadApp("com.oplus.cosa", HookOplusCosa)
        //软件更新
        loadApp("com.oplus.ota", HookOplusOta)
        //系统升级服务
        //乐划锁屏
        loadApp("com.heytap.pictorial", HookPictorial)
        //信息
        loadApp("com.android.mms", HookOplusMMS)
        //电话
        //电话服务
        loadApp("com.android.phone", HookPhone)
        //浏览器
        loadApp("com.heytap.browser", HookBrowser)
        //手势体感
        loadApp("com.oplus.gesture", HookGesture)
        //权限控制器
        loadApp("com.android.permissioncontroller", HookPermissionController)
        //小布助手
        loadApp("com.heytap.speechassist", HookSpeechAssist)
        //小布识屏
        loadApp("com.coloros.directui", HookDirectUI)
        //全局搜索
        loadApp("com.heytap.quicksearchbox", HookQuickSearchBox)
        //软件商店
        loadApp("com.heytap.market", HookMarket)
        //天气
        loadApp("com.coloros.weather2", HookWeather)
        //日历
        loadApp("com.coloros.calendar", HookCalendar)
        //智能侧边栏
        loadApp("com.coloros.smartsidebar", HookSmartSidebar)
        //手机管家
        loadApp("com.coloros.phonemanager", HookPhoneManager)
        //支付保护
        loadApp("com.coloros.securepay", HookSecurePay)
        //应用分身
        loadApp("com.oplus.multiapp", HookMultiApp)
        //录音
        loadApp("com.coloros.soundrecorder", HookSoundRecorder)
        //三方应用通话录音 / 智慧语音
        loadApp("com.oplus.audiomonitor", HookAudioMonitor)
        //atlasService
        //audioEffectCenter
        loadApp("com.oplus.audio.effectcenter", HookAudioEffectCenter)
        //护眼模式
        //MediaController
        loadApp("com.oplus.mediacontroller", HookMediaController)
        //无网畅聊
        loadApp("com.oplus.beaconlink", HookBeaconLink)
        //无线设置
        loadApp("com.oplus.wirelesssettings", HookWirelessSettings)
        //健康
        loadApp("com.heytap.health", HookHealth)
        //NFC服务
        loadApp("com.android.nfc", HookNfc)
        //互传
        loadApp("com.coloros.oshare", HookOShare)
        //蓝牙
        //电话本
        //权限管理
        loadApp("com.oplus.securitypermission", HookSecuritypPermission)
        //文件管理
        loadApp("com.coloros.filemanager", HookFileManager)
        //工程模式
        loadApp("com.oplus.engineermode", HookEngineerMode)
        //设备空间
        //设备快连
        //系统消息

        //其他APP
        loadApp("com.theappninjas.fakegpsjoystick", HookFakeGpsJoyStick)
        loadApp("com.ruet_cse_1503050.ragib.appbackup.pro", HookAlphaBackupPro)
        loadApp("ru.kslabs.ksweb", HookKsWeb)
        loadApp("com.dv.adm", HookADM)
    }
}