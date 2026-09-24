package com.luckyzyx.luckytool.hook.scopes.nfc

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ScanNfcTagAutoClick : YukiBaseHooker() {
    override fun onHook() {
        var isEnable = preferences(ModulePrefs).getBoolean("scan_nfc_tag_auto_click", false)
        dataChannel.wait<Boolean>("scan_nfc_tag_auto_click") { isEnable = it }

        //Source TagDetectedNotification
        "com.oplus.nfc.dispatch.TagDetectedNotification".toClass().resolve().apply {
            firstMethod { name = "show" }.hook {
                before {
                    if (!isEnable) return@before
                    val context = firstArg().get<Context>() ?: return@before
                    val intent = arg(1).get<Intent>() ?: return@before
                    val type = arg(2).get<Int>() ?: 0
//                    val bundle = lastArg().get<Bundle>()

                    val pendingIntent = Intent().apply {
                        setAction("com.oplus.nfc.dispatch.TagDetectedNotification.ACTION_PROCESS_TAG")
                        putExtra("dispatcherIntent", intent)
                        putExtra("componentType", type)
                        setPackage("com.android.nfc")
                    }
                    PendingIntent.getBroadcast(
                        context, System.currentTimeMillis().toInt(), pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    ).send()
                }
            }
        }
    }
}