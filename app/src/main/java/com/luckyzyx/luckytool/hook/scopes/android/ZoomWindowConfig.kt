package com.luckyzyx.luckytool.hook.scopes.android

import android.os.Bundle
import android.util.ArraySet
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.XLog
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class ZoomWindowConfig : Hooker {

    var callback: ((key: String, value: Any) -> Unit)? = null

    var mode = "0"
    val list = ArraySet<String>()

    var multiWindow = false
    var multiNum = 2

    fun loadData() {
        mode = prefs(ModulePrefs).getString("custom_app_floating_window_display_mode", "0")
        list.addAll(prefs(ModulePrefs).getStringSet("zoom_window_support_list", ArraySet()))

        dataChannel.wait<String>("custom_app_floating_window_display_mode") {
            mode = it
            XLog.debug("update zoom window configs status -> $it")
        }

        dataChannel.watch("zoom_window_support_list") {
            val new = prefs(ModulePrefs).getStringSet("zoom_window_support_list", ArraySet())
            XLog.debug("update zoom window whitelist configs -> ${list.size} | ${new.size}")
            list.clear()
            list.addAll(new)
        }

        multiWindow = prefs(ModulePrefs).getBoolean("force_enable_multi_window_mode", false)
        dataChannel.wait<Boolean>("force_enable_multi_window_mode") { multiWindow = it }
        multiNum = prefs(ModulePrefs).getInt("custom_multi_window_display_upper_limit", 2)
        dataChannel.wait<Int>("custom_multi_window_display_upper_limit") { multiNum = it }

        XLog.debug("init zoom window configs success -> ${list.size}")
    }

    override fun onHook() {
        loadData()

        val osCode = getOSVersionCode
        loadHooker(HookZoomWindow())
        if (osCode >= 33) loadHooker(HookFlexibleWindow())
    }

    @Obfuscate
    inner class HookZoomWindow : Hooker {
        override fun onHook() {
            //Source OplusZoomWindowConfig
            "com.android.server.wm.OplusZoomWindowConfig".toClass().resolve().apply {
                firstMethod {
                    name = "isSupportZoomMode"
                    parameters(String::class, Int::class, String::class, Bundle::class)
                }.hook {
                    before {
                        when (mode) {
                            "1" -> resultFalse()
                            "2" -> resultTrue()
                            "3" -> {
                                val target = args().first().string()
                                val packName = if (target.contains("/").not()) target
                                else target.split("/")[0]
                                if (list.contains(packName)) resultTrue()
                            }
                        }
                    }
                }
            }
        }
    }

    @Obfuscate
    inner class HookFlexibleWindow : Hooker {
        override fun onHook() {
            //Source FlexibleWindowUtils
            "com.android.server.wm.FlexibleWindowUtils".toClassOrNull()?.resolve()?.apply {
                firstMethod {
                    name = "isSupportFlexibleWindow"
                    parameters(String::class, String::class)
                }.hook {
                    before {
                        when (mode) {
                            "1" -> resultFalse()
                            "2" -> resultTrue()
                            "3" -> {
                                val target = args().first().string()
                                val packName = if (target.contains("/").not()) target
                                else target.split("/")[0]
                                if (list.contains(packName)) resultTrue()
                            }
                        }
                    }
                }
            }

            //Source FlexibleWindowManagerService
            "com.android.server.wm.FlexibleWindowManagerService".toClassOrNull()?.resolve()?.apply {
                firstMethod {
                    name = "getMaxWinNum"
                    returnType = Int::class
                }.hook {
                    after {
//                        val scenario = args().first().int()
//                        val num = result<Int>() ?: -1
//                        YLog.debug("${method.name} -> $scenario -> $num")
                        if (multiWindow && multiNum > 0) result = multiNum
                    }
                }
            }
        }
    }
}