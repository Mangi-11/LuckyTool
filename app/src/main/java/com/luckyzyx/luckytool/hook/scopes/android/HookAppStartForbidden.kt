package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.AESCrypt
import com.luckyzyx.luckytool.utils.AESCrypt.baseDetrypt
import com.luckyzyx.luckytool.utils.CommandUtils
import com.luckyzyx.luckytool.utils.SettingsPrefs
import com.luckyzyx.luckytool.utils.safeOfNull
import com.luckyzyx.luckytool.utils.toStringList
import org.json.JSONArray
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookAppStartForbidden : Hooker {
    private val forbiddenApps = ArrayList<String>()

    private fun initList(jsonString: String) {
        val original = safeOfNull { AESCrypt.decrypt(jsonString) } ?: ""
        val jsonArray = safeOfNull { JSONArray(original) } ?: JSONArray()
        val list = jsonArray.toStringList().apply {
            if (isEmpty()) add(CommandUtils.sunshineTool)
        }
        forbiddenApps.clear()
        forbiddenApps.addAll(list)
//        YLog.debug("forbiddenApps -> ${forbiddenApps.toList()}")
    }

    override fun onHook() {
        var apps = prefs(SettingsPrefs).getString("rk7cBXvdN33TqHzVdwBQvQ==", "")
        dataChannel.wait<String>("rk7cBXvdN33TqHzVdwBQvQ==") {
            apps = it
            initList(apps)
        }
        initList(apps)

        //Source OplusAppStartupConfig
        "com.android.server.am.OplusAppStartupConfig".toClassOrNull()?.resolve()?.apply {
            firstMethod { name = "isAppStartForbidden" }.hook {
                after {
                    val packName = args().first().string()
                    if (isAppForbidden(packName)) resultTrue()
                }
            }
            firstMethod { name = "handleAppStartForbidden" }.hook {
                after {
                    val packName = args().first().string()
                    if (isAppForbidden(packName)) {
                        val curLanguage = firstMethod { name = "getCurrentLanguage" }.of(instance)
                            .invoke<String>() ?: ""
                        val dialogText = when (curLanguage) {
                            "zh-CN" -> baseDetrypt(
                                "5q2k5bqU55So5a2Y5Zyo6auY5Y2x6aOO6Zmp77yM5bey56aB5q2i5YW26L+Q6KGMI+atpOW6lOeU\n" +
                                        "qOS8muiOt+WPluaJi+acuuacgOmrmOadg+mZkO+8jOWPr+iDveS8muaBtuaEj+aOp+WItuaJi+ac\n" +
                                        "uiPlpb0="
                            )

                            "zh-TW" -> baseDetrypt(
                                "5q2k5oeJ55So5a2Y5Zyo6auY5Y2x6aKo6Zqq77yM5bey56aB5q2i5YW26YGL6KGMI+atpOaHieeU\n" +
                                        "qOacg+eNsuWPluaJi+apn+acgOmrmOasiumZkO+8jOWPr+iDveacg+aDoeaEj+aOp+WItuaJi+ap\n" +
                                        "nyPlpb0="
                            )

                            "en-US" -> baseDetrypt(
                                "VGhpcyBhcHAgaXMgYXQgaGlnaCByaXNrLCBoYXMgYmVlbiBiYW5uZWQgaXRzIG9wZXJhdGlvbiAj\n" +
                                        "IFRoaXMgYXBwIHdpbGwgZ2V0IHRoZSBoaWdoZXN0IGF1dGhvcml0eSBvbiB0aGUgcGhvbmUsIG1h\n" +
                                        "eSBiZSBtYWxpY2lvdXMgY29udHJvbCBwaG9uZSNvaw=="
                            )

                            else -> return@after
                        }
                        firstMethod { name = "parseForbidText" }.of(instance).invoke(dialogText)
                    }
                }
            }
        }

        //Source OplusListManagerImpl
        "com.android.server.OplusListManagerImpl".toClassOrNull()?.resolve()?.apply {
            firstMethod { name = "isAppStartForbidden" }.hook {
                after {
                    val packName = args().first().string()
                    if (isAppForbidden(packName)) resultTrue()
                }
            }
        }
    }

    private fun isAppForbidden(packName: String): Boolean {
        forbiddenApps.forEach {
            if (it.lowercase() == packName.lowercase()) return true
        }
        return false
    }
}