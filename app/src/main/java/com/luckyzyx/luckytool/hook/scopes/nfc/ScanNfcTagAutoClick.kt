package com.luckyzyx.luckytool.hook.scopes.nfc

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ScanNfcTagAutoClick : Hooker {
    override fun onHook() {
        var isEnable = prefs(ModulePrefs).getBoolean("scan_nfc_tag_auto_click", false)
        dataChannel.wait<Boolean>("scan_nfc_tag_auto_click") { isEnable = it }

        //Source TagDetectedNotification
        "com.oplus.nfc.dispatch.TagDetectedNotification".toClass().resolve().apply {
            firstMethod { name = "show" }.hook {
                before {
                    if (!isEnable) return@before
                    val context = args().first().cast<Context>() ?: return@before
                    val intent = args(1).cast<Intent>() ?: return@before
                    val type = args(2).int()
//                    val bundle = args().last().cast<Bundle>()

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