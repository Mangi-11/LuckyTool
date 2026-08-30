package com.luckyzyx.luckytool.hook.scopes.systemui

import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.getOSVersionCode
import com.luckyzyx.luckytool.utils.safeOfNull
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object StatusBarBatteryView : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode
        if (osCode >= 30) loadHooker(StatusBarPowerStyle)
        else loadHooker(StatusBarPowerStyleC13)
    }

    @Obfuscate
    object StatusBarPowerStyle : Hooker {
        override fun onHook() {
            val removePercent =
                prefs(ModulePrefs).getBoolean("remove_statusbar_battery_percent", false)
            val userTypeface = prefs(ModulePrefs).getBoolean("statusbar_power_user_typeface", false)
            val useBoldFont =
                prefs(ModulePrefs).getBoolean("statusbar_power_use_bold_font_style", false)
            val customFontSize = prefs(ModulePrefs).getInt("statusbar_power_font_size", 0)
            val applyToIcon =
                prefs(ModulePrefs).getBoolean("statusbar_power_apply_to_battery_icon", false)

            //Source BatteryViewBinder
            "com.oplus.systemui.statusbar.pipeline.battery.ui.binder.BatteryViewBinder".toClass()
                .resolve().apply {
                    firstMethodOrNull { name = "bind\$initView" }?.hook {
                        after {
                            args.filterIsInstance<TextView>().forEachIndexed { _, view ->
                                view.handBatteryTextView(
                                    removePercent,
                                    userTypeface,
                                    useBoldFont,
                                    customFontSize,
                                    applyToIcon
                                )
                            }
                        }
                    }
                    firstMethodOrNull { name = "updateText" }?.hook {
                        after {
                            val view = args().first().cast<TextView>() ?: return@after
                            view.handBatteryTextView(
                                removePercent,
                                userTypeface,
                                useBoldFont,
                                customFontSize,
                                applyToIcon
                            )
                        }
                    }

                    firstMethodOrNull { name = "bind\$updateOldHorizontal" }?.hook {
                        after {
                            args.filterIsInstance<TextView>().forEachIndexed { _, view ->
                                view.handBatteryTextView(
                                    removePercent,
                                    userTypeface,
                                    useBoldFont,
                                    customFontSize,
                                    applyToIcon
                                )
                            }
                        }
                    }

                    firstMethodOrNull { name = "bind\$updatePercentOutView" }?.hook {
                        after {
                            args.filterIsInstance<TextView>().forEachIndexed { _, view ->
                                view.handBatteryTextView(
                                    removePercent,
                                    userTypeface,
                                    useBoldFont,
                                    customFontSize,
                                    applyToIcon
                                )
                            }
                        }
                    }
                }

            //Source StatBatteryMeterView
            "com.oplus.systemui.statusbar.pipeline.battery.ui.view.StatBatteryMeterView".toClass()
                .resolve().apply {
                    (firstMethodOrNull { name = "setTextTypeface" }
                        ?: firstMethod { name = "setFontTypeface" }).hook {
                        if (userTypeface) intercept()
                    }
                }
        }
    }

    @Obfuscate
    object StatusBarPowerStyleC13 : Hooker {
        override fun onHook() {
            val removePercent =
                prefs(ModulePrefs).getBoolean("remove_statusbar_battery_percent", false)
            val userTypeface = prefs(ModulePrefs).getBoolean("statusbar_power_user_typeface", false)
            val useBoldFont =
                prefs(ModulePrefs).getBoolean("statusbar_power_use_bold_font_style", false)
            val customFontSize = prefs(ModulePrefs).getInt("statusbar_power_font_size", 0)
            val applyToIcon =
                prefs(ModulePrefs).getBoolean("statusbar_power_apply_to_battery_icon", false)

            //Source StatBatteryMeterView
            "com.oplusos.systemui.statusbar.widget.StatBatteryMeterView".toClass().resolve().apply {
                firstMethod { name = "onConfigChanged" }.hook {
                    after {
                        firstMethod { name = "updatePercentText" }.of(instance).invoke()
                    }
                }
                firstMethod { name = "updatePercentText" }.hook {
                    after {
                        if (applyToIcon) firstField { name = "batteryPercentView" }.of(instance)
                            .get<TextView>()?.handBatteryTextView(
                                removePercent, userTypeface, useBoldFont, customFontSize, true
                            )
                        firstField { name = "batteryPercentText" }.of(instance).get<TextView>()
                            ?.handBatteryTextView(
                                removePercent,
                                userTypeface,
                                useBoldFont,
                                customFontSize,
                                applyToIcon
                            )
                    }
                }
            }
        }
    }

    fun TextView.handBatteryTextView(
        removePercent: Boolean, userTypeface: Boolean, useBoldFont: Boolean,
        customFontSize: Int, applyToIcon: Boolean
    ) {
        val entryName = safeOfNull { resources.getResourceEntryName(id) }
        when (entryName) {
            "battery_text" -> if (!applyToIcon) return

            "battery_percentage_view" -> {}
            else -> return
        }
        if (removePercent) text = text.toString().replace("%", "")
        if (userTypeface) {
            typeface = if (useBoldFont) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            setTextSize(
                TypedValue.COMPLEX_UNIT_DIP,
                if (customFontSize == 0) 12F else customFontSize.toFloat() * 2
            )
        }
    }
}