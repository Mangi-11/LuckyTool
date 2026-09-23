package com.luckyzyx.luckytool.hook.scopes.nfc

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ScanNfcTagAutoClick : Hooker {
    override fun onHook() {
        var isEnable = prefs(ModulePrefs).getBoolean("scan_nfc_tag_auto_click", false)
        dataChannel.wait<Boolean>("scan_nfc_tag_auto_click") { isEnable = it }

        if (getOSVersionCode >= 40) {
            // ColorOS 17 moved the notification flow out of TagDetectedNotification.show.
            "com.oplus.nfc.dispatch.OplusNotificationManager".toClass().resolve().apply {
                firstMethod { name = "showNotificationIfNeeded"; parameterCount = 4 }.hook {
                    after {
                        if (!isEnable || result != true || hasThrowable()) return@after
                        val context = firstField { name = "mContext" }.of(instance).get<Context>()
                            ?: return@after
                        val intent = args(3).cast<Intent>() ?: return@after
                        sendProcessTagBroadcast(context, intent)
                    }
                }
            }
            return
        }

        //Source TagDetectedNotification
        "com.oplus.nfc.dispatch.TagDetectedNotification".toClass().resolve().apply {
            firstMethod { name = "show" }.hook {
                before {
                    if (!isEnable) return@before
                    val context = args().first().cast<Context>() ?: return@before
                    val intent = args(1).cast<Intent>() ?: return@before
                    val type = args(2).int()
                    sendProcessTagBroadcast(context, intent, type)
                }
            }
        }
    }

    private fun sendProcessTagBroadcast(context: Context, intent: Intent, type: Int? = null) {
        val pendingIntent = Intent().apply {
            action = "com.oplus.nfc.dispatch.TagDetectedNotification.ACTION_PROCESS_TAG"
            putExtra("dispatcherIntent", intent)
            if (type != null) putExtra("componentType", type)
            setPackage("com.android.nfc")
        }
        PendingIntent.getBroadcast(
            context, System.currentTimeMillis().toInt(), pendingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ).send()
    }
}
