package com.luckyzyx.luckytool.hook.scopes.systemui

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.isSubclassOf
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.injectModuleAppResources
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.R
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.closeScreen
import com.luckyzyx.luckytool.utils.getOSVersionCode
import com.luckyzyx.luckytool.utils.safeOfNull
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object LockScreenBottomButton : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode
        if (osCode >= 37) loadHooker(FlashlightQuickCloseScreen)
        else if (osCode >= 30) loadHooker(LockScreenBottomButtonV14)
        else loadHooker(LockScreenBottomButtonV13)
    }

    @Obfuscate
    object FlashlightQuickCloseScreen : Hooker {
        override fun onHook() {
            var autoCloseScreen = prefs(ModulePrefs).getBoolean(
                "lock_screen_switch_flashlight_auto_close_screen", false
            )
            dataChannel.wait<Boolean>("lock_screen_switch_flashlight_auto_close_screen") {
                autoCloseScreen = it
            }

            //Source OplusFlashlightQuickAffordanceConfig
            "com.oplus.systemui.keyguard.data.quickaffordance.OplusFlashlightQuickAffordanceConfig".toClass()
                .resolve().apply {
                    firstMethod { name = "onTriggered" }.hook {
                        after {
                            if (!autoCloseScreen) return@after
                            val context = firstField { name = "context";superclass() }.of(instance)
                                .get<Context>() ?: return@after
                            closeScreen(context)
                        }
                    }
                }
        }
    }

    @Obfuscate
    object LockScreenBottomButtonV14 : Hooker {
        val ViewModel =
            "com.android.systemui.keyguard.ui.viewmodel.KeyguardQuickAffordanceViewModel"

        override fun onHook() {
            var rmLeft =
                prefs(ModulePrefs).getBoolean("remove_lock_screen_bottom_left_button", false)
            dataChannel.wait<Boolean>("remove_lock_screen_bottom_left_button") { rmLeft = it }
            var rmRight =
                prefs(ModulePrefs).getBoolean("remove_lock_screen_bottom_right_camera", false)
            dataChannel.wait<Boolean>("remove_lock_screen_bottom_right_camera") { rmRight = it }
            var autoCloseScreen = prefs(ModulePrefs).getBoolean(
                "lock_screen_switch_flashlight_auto_close_screen", false
            )
            dataChannel.wait<Boolean>("lock_screen_switch_flashlight_auto_close_screen") {
                autoCloseScreen = it
            }

            //Source KeyguardBottomAreaViewBinder
            "com.android.systemui.keyguard.ui.binder.KeyguardBottomAreaViewBinder".toClass()
                .resolve().apply {
                    (firstMethodOrNull { name = "updateButton" }
                        ?: firstMethod { name { it.endsWith("updateButton") } }).hook {
                        before {
                            val viewModel = args.find {
                                it != null && it::class isSubclassOf ViewModel.toClass()
                            } ?: return@before
                            val solt = viewModel.asResolver().firstField { name = "slotId" }
                                .get<String>() ?: ""
                            when (solt) {
                                "bottom_start" -> if (rmLeft) {
                                    viewModel.asResolver().firstField { name = "isVisible" }
                                        .set(false)
                                }

                                "bottom_end" -> if (rmRight) {
                                    viewModel.asResolver().firstField { name = "isVisible" }
                                        .set(false)
                                }
                            }
                        }
                    }
                }

            //Source OplusFlashlightQuickAffordanceConfig
            "com.oplus.systemui.keyguard.data.quickaffordance.OplusFlashlightQuickAffordanceConfig".toClass()
                .resolve().apply {
                    firstMethod { name = "onTriggered" }.hook {
                        after {
                            if (rmLeft || !autoCloseScreen) return@after
                            val context = firstField { name = "context";superclass() }.of(instance)
                                .get<Context>() ?: return@after
                            closeScreen(context)
                        }
                    }
                }
        }
    }

    @Obfuscate
    object LockScreenBottomButtonV13 : Hooker {
        override fun onHook() {
            //affordance_magazine
            var rmLeft =
                prefs(ModulePrefs).getBoolean("remove_lock_screen_bottom_left_button", false)
            dataChannel.wait<Boolean>("remove_lock_screen_bottom_left_button") { rmLeft = it }
            //affordance_camera
            var rmRight =
                prefs(ModulePrefs).getBoolean("remove_lock_screen_bottom_right_camera", false)
            dataChannel.wait<Boolean>("remove_lock_screen_bottom_right_camera") { rmRight = it }

            //affordance_flashlight
            var useFlashLight = prefs(ModulePrefs).getBoolean(
                "lock_screen_bottom_left_button_replace_with_flashlight", false
            )
            dataChannel.wait<Boolean>("lock_screen_bottom_left_button_replace_with_flashlight") {
                useFlashLight = it
            }
            var autoCloseScreen = prefs(ModulePrefs).getBoolean(
                "lock_screen_switch_flashlight_auto_close_screen", false
            )
            dataChannel.wait<Boolean>("lock_screen_switch_flashlight_auto_close_screen") {
                autoCloseScreen = it
            }

            //Source KeyguardBottomAreaView
            "com.android.systemui.statusbar.phone.KeyguardBottomAreaView".toClass().resolve()
                .apply {
                    firstMethod { name = "onFinishInflate" }.hook {
                        before {
                            if (!useFlashLight) return@before
                            instance<ViewGroup>().context.injectModuleAppResources()
                        }
                    }
                    firstMethod { name = "updateLeftAffordanceIcon" }.hook {
                        after {
                            if (!useFlashLight) return@after
                            val context = instance<ViewGroup>().context
                            firstMethod { name = "updateLeftAffordanceVisibility" }.of(instance)
                                .invoke()
                            val mFlashlightController =
                                firstField { name = "mFlashlightController" }.of(instance).get()
                            val isEnable = mFlashlightController?.asResolver()?.firstMethod {
                                name = "isEnabled"
                            }?.invoke<Boolean>() ?: false
                            val resId = if (isEnable) R.drawable.affordance_flashlight_on
                            else R.drawable.affordance_flashlight
                            val drawable = safeOfNull {
                                ResourcesCompat.getDrawable(context.resources, resId, null)
                            }
                            firstField { name = "mLeftAffordanceView";superclass() }.of(instance)
                                .get()?.asResolver()?.firstMethod {
                                    name = "setImageDrawable"
                                    parameters(Drawable::class, Boolean::class)
                                    superclass()
                                }?.invoke(drawable, !isEnable)
                        }
                    }
                    firstMethod { name = "updateLeftAffordanceVisibility" }.hook {
                        after {
                            if (rmLeft) {
                                firstField {
                                    name = "mLeftAffordanceView";superclass()
                                }.of(instance).get<View>()?.isVisible = false
                                return@after
                            }
                            if (useFlashLight) {
                                firstField {
                                    name = "mLeftAffordanceView";superclass()
                                }.of(instance).get<ImageView>()?.isVisible = true
                            }
                        }
                    }
                    firstMethod { name = "launchLeftAffordance" }.hook {
                        before {
                            if (!useFlashLight) return@before
                            firstMethod { name = "baseLaunchLeftAffordance";superclass() }.of(
                                instance
                            ).invoke()
                            val mFlashlightController =
                                firstField { name = "mFlashlightController" }.of(instance).get()
                            val isEnable = mFlashlightController?.asResolver()?.firstMethod {
                                name = "isEnabled"
                            }?.invoke<Boolean>() ?: true
                            mFlashlightController?.asResolver()
                                ?.firstMethod { name = "setFlashlight" }
                                ?.invoke(!isEnable)
                            firstMethod { name = "updateLeftAffordanceIcon" }.of(instance).invoke()
                            if (autoCloseScreen) closeScreen(instance<ViewGroup>().context)
                            resultNull()
                        }
                    }
                    firstMethod { name = "updateCameraVisibility" }.hook {
                        before {
                            if (!rmRight) return@before
                            firstField { name = "mRightAffordanceView";superclass() }.of(instance)
                                .get<ImageView>()?.isVisible = false
                            resultNull()
                        }
                    }
                }
        }
    }
}