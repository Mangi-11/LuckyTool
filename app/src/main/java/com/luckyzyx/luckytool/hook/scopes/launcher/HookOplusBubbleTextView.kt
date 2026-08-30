package com.luckyzyx.luckytool.hook.scopes.launcher

import android.widget.TextView
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.dp
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookOplusBubbleTextView : Hooker {

    override fun onHook() {
        val osCode = getOSVersionCode

        val multiLine =
            prefs(ModulePrefs).getBoolean("allow_app_names_display_multiple_lines", false)
        val textLineHeight = prefs(ModulePrefs).getInt("custom_app_icon_name_line_height", -1)
        val iconSize = prefs(ModulePrefs).getInt("custom_launcher_app_icon_size", 0)

        //Source OplusBubbleTextView
        "com.android.launcher3.OplusBubbleTextView".toClass().resolve().apply {
            if (osCode < 26 && multiLine) {
                firstMethod {
                    name = "setMaxLines"
                    parameters(Int::class)
                }.hook {
                    before {
                        instance<TextView>().maxLines = 2
                        resultNull()
                    }
                }
            }
            if (multiLine && textLineHeight > -1) {
                firstConstructor { parameterCount = 3 }.hook {
                    after {
                        instance<TextView>().apply {
                            lineHeight = textLineHeight.dp
                        }
                    }
                }
            }
        }

        //Source IconParam
        "com.android.launcher.layoutparam.IconParam".toClass().resolve().apply {
            firstMethod { name = "getIconSizePx" }.hook {
                before {
                    if (iconSize > 0) result = iconSize.dp
                }
            }
        }
    }
}