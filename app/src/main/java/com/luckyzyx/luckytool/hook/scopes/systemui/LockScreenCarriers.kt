package com.luckyzyx.luckytool.hook.scopes.systemui

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.utils.A14
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object LockScreenCarriers : Hooker {
    override fun onHook() {
        if (SDK >= A14) loadHooker(LockScreenCarrier)
        else loadHooker(LockScreenCarrierV13)
    }

    @Obfuscate
    private object LockScreenCarrier : Hooker {
        override fun onHook() {
            val isRemove = prefs(ModulePrefs).getBoolean("remove_statusbar_carriers", false)
            val customText =
                prefs(ModulePrefs).getString("statusbar_custom_carrier_display_text", "")
            val userFont =
                prefs(ModulePrefs).getBoolean("statusbar_carriers_use_user_typeface", false)


            //Source OplusCarrierTextCallbackInfo
            "com.oplus.systemui.qs.OplusQSCarrierTextController\$OplusCarrierTextCallbackInfo".toClass()
                .resolve().apply {
                    constructor {}.hookAll {
                        after {
                            if (customText.isNotBlank()) {
                                firstField { name = "carrierText" }.of(instance).set(customText)
                            }
                        }
                    }
                }

            //Source OplusStatCarrierTextController
            "com.oplus.systemui.statusbar.widget.OplusStatCarrierTextController".toClass().resolve()
                .apply {
                    firstMethod { name = "onViewAttached" }.hook {
                        after {
                            firstMethod { name = "setVisible" }.of(instance).invoke(false)
                        }
                    }
                    firstMethod { name = "setVisible" }.hook {
                        before {
                            if (isRemove) args().first().setFalse()
                        }
                    }
                    firstMethod { name = "updateCarrierInfo" }.hook {
                        after {
                            if (isRemove) firstMethod { name = "setVisible" }.of(instance)
                                .invoke(false)
                        }
                    }
                }

            //Source OplusStatCarrierText
            "com.oplus.systemui.statusbar.widget.OplusStatCarrierText".toClass().resolve().apply {
                firstConstructor { parameterCount = 2 }.hook {
                    after {
                        if (userFont) instance<TextView>().typeface = Typeface.DEFAULT_BOLD
                    }
                }
                firstMethod { name = "onConfigurationChanged" }.hook {
                    after {
                        if (customText.isNotBlank()) instance<TextView>().text = customText
                        if (userFont) instance<TextView>().typeface = Typeface.DEFAULT_BOLD
                    }
                }
            }
        }
    }

    @Obfuscate
    private object LockScreenCarrierV13 : Hooker {
        override fun onHook() {
            val userFont =
                prefs(ModulePrefs).getBoolean("statusbar_carriers_use_user_typeface", false)
            val isRemove = prefs(ModulePrefs).getBoolean("remove_statusbar_carriers", false)
            val customText =
                prefs(ModulePrefs).getString("statusbar_custom_carrier_display_text", "")

            //Source StatOperatorNameView
            "com.oplusos.systemui.statusbar.widget.StatOperatorNameView".toClass().resolve().apply {
                firstConstructor { parameterCount = 3 }.hook {
                    after {
                        if (userFont) instance<TextView>().typeface = Typeface.DEFAULT_BOLD
                    }
                }
                firstMethod { name = "onConfigurationChanged" }.hook {
                    after {
                        if (userFont) instance<TextView>().typeface = Typeface.DEFAULT_BOLD
                    }
                }
                firstMethod { name = "updateCarrierInfo";superclass() }.hook {
                    after {
                        instance<TextView>().apply {
                            if (isRemove) isVisible = false
                            if (customText.isNotBlank()) text = customText
                        }
                    }
                }
            }
        }
    }
}