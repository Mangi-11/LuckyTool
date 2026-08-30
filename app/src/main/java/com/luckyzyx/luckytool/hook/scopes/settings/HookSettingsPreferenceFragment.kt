package com.luckyzyx.luckytool.hook.scopes.settings

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookSettingsPreferenceFragment : Hooker {
    override fun onHook() {
        //启用应用专属媒体音量
        val specificMediaVolume =
            prefs(ModulePrefs).getBoolean("enable_app_specific_media_volume", false)

        //Source SettingsPreferenceFragment
        "com.android.settings.SettingsPreferenceFragment".toClass().resolve().apply {
            firstMethod {
                name = "removePreference"
                parameters(String::class)
            }.hook {
                before {
                    when (args().first().string()) {
                        "voice_mode_category" -> if (specificMediaVolume) resultTrue()
                    }
                }
            }
        }
    }
}