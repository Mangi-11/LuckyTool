package com.luckyzyx.luckytool.hook.scopes.settings

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookIris5Controller : Hooker {
    override fun onHook() {
        val isVideoFrameInsertion = true
        //prefs(ModulePrefs).getBoolean("video_display_enhancement_support_2K120", false)
        val isVideoDisplayEnhancement = true
        //prefs(ModulePrefs).getBoolean("video_super_resolution_support_2K120", false)
        val isVideoSuperResolution = true

        //Source Iris5MotionFluencySwitchController
        "com.oplus.settings.feature.display.controller.Iris5MotionFluencySwitchController".toClass()
            .resolve().apply {
                firstMethod { name = "is2kReject" }.hook {
                    if (isVideoFrameInsertion) replaceToFalse()
                }
                firstMethod { name = "isSupport120With2K" }.hook {
                    if (isVideoFrameInsertion) replaceToTrue()
                }
            }
        //Source Iris5MotionFluencyController
        "com.oplus.settings.feature.display.controller.Iris5MotionFluencyController".toClass()
            .resolve().apply {
                firstMethod { name = "is2kReject" }.hook {
                    if (isVideoFrameInsertion) replaceToFalse()
                }
                firstMethod { name = "isSupport120With2K" }.hook {
                    if (isVideoFrameInsertion) replaceToTrue()
                }
            }
        //Source Iris5VideoDisplayEnhancementController
        "com.oplus.settings.feature.display.controller.Iris5VideoDisplayEnhancementController".toClass()
            .resolve().apply {
                firstMethod { name = "is2kReject" }.hook {
                    if (isVideoDisplayEnhancement) replaceToFalse()
                }
                firstMethod { name = "isSupport120With2K" }.hook {
                    if (isVideoDisplayEnhancement) replaceToTrue()
                }
            }
        //Source Iris5VideoSuperResolutionController
        "com.oplus.settings.feature.display.controller.Iris5VideoSuperResolutionController".toClass()
            .resolve().apply {
                firstMethod { name = "is2kReject" }.hook {
                    if (isVideoSuperResolution) replaceToFalse()
                }
                firstMethod { name = "isSupport120With2K" }.hook {
                    if (isVideoSuperResolution) replaceToTrue()
                }
            }
    }
}