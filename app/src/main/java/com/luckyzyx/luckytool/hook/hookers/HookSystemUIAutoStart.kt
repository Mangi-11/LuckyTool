package com.luckyzyx.luckytool.hook.hookers

import android.app.StatusBarManager
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Handler
import com.drake.net.utils.scope
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.luckyzyx.luckytool.BuildConfig
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.XLog
import com.luckyzyx.luckytool.hook.core.onAppLifecycle
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.convertToMillis
import kotlinx.coroutines.delay
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookSystemUIAutoStart : Hooker {
    override fun onHook() {
        var nfcEnable = prefs(ModulePrefs).getBoolean("enable_nfc_delay_shutdown", false)
        dataChannel.wait<Boolean>("enable_nfc_delay_shutdown") { nfcEnable = it }
        var nfcDelay = prefs(ModulePrefs).getString("custom_nfc_delay_shutdown_time", "10M")
        dataChannel.wait<String>("custom_nfc_delay_shutdown_time") { nfcDelay = it }

        onAppLifecycle {
            //监听锁屏解锁
            registerReceiver(Intent.ACTION_USER_PRESENT) { context, _ ->
                scope {
                    delay(200)
                    try {
                        context.startForegroundService(Intent().apply {
                            action = "${BuildConfig.APPLICATION_ID}.AutoStartControllerService"
                            setPackage(BuildConfig.APPLICATION_ID)
                        })
                    } catch (t: Throwable) {
                        XLog.debug("AutoStartService try sthrow", t)
                    }
                }.catch {
                    XLog.debug("AutoStartService scope throw", it)
                }
            }
            //监听模块磁贴关闭控制中心
            registerReceiver("LuckyTool_CloseCollapse") { context, _ ->
                val service = context.getSystemService(StatusBarManager::class.java)
                service.asResolver().firstMethod { name = "collapsePanels" }.invoke()
            }
            //监听NFC启用状态
            registerReceiver(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) { context, intent ->
                if (!nfcEnable) return@registerReceiver
                val delay = convertToMillis(nfcDelay)
                if (delay < 0) {
                    nfcEnable = false
                    XLog.debug("NFC Delay Error -> $nfcDelay | $delay")
                    return@registerReceiver
                }
                val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
                val intExtra = intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 1)
                val handler = Handler(context.mainLooper)
                val runnable = Runnable {
                    nfcAdapter.asResolver().firstMethod { name = "disable" }.invoke()
                }
                if (nfcAdapter.isEnabled) {
                    try {
                        if (handler.hasCallbacks(runnable)) return@registerReceiver
                        handler.postDelayed(runnable, delay)
                    } catch (t: Throwable) {
                        XLog.debug("NFC [$intExtra] Handler Add Error", t)
                    }
                } else {
                    try {
                        if (handler.hasCallbacks(runnable)) handler.removeCallbacks(runnable)
                    } catch (t: Throwable) {
                        XLog.debug("NFC [$intExtra] Handler Remove Error", t)
                    }
                }
            }
        }
    }
}
