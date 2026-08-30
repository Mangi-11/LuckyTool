package com.luckyzyx.luckytool.hook.scopes.systemui

import android.content.Context
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.hook.core.toClass
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object FingerPrintIconAnim : Hooker {

    private val fpIconType = VariousClass(
        "com.oplusos.systemui.keyguard.onscreenfingerprint.OnScreenFingerprintIcon", //C12
        "com.oplus.systemui.keyguard.finger.onscreenfingerprint.OnScreenFingerprintIcon",  //C13
        "com.oplus.systemui.biometrics.finger.udfps.OnScreenFingerprintIcon" //C14 C15
    )

    override fun onHook() {
        val removeMode = prefs(ModulePrefs).getString("remove_fingerprint_icon_mode", "0")
        val isReplaceIcon = prefs(ModulePrefs).getBoolean("replace_fingerprint_icon_switch", false)
        val iconPath = prefs(ModulePrefs).getString("replace_fingerprint_icon_path", "")

        //Source OnScreenFingerprintUiMech
        VariousClass(
            "com.oplusos.systemui.keyguard.onscreenfingerprint.OnScreenFingerprintOpticalAnimCtrl", //C12
            "com.oplus.systemui.keyguard.finger.onscreenfingerprint.OnScreenFingerprintUiMech", //C13
            "com.oplus.systemui.biometrics.finger.udfps.OnScreenFingerprintUiMach", //C14
            "com.oplus.systemui.biometrics.finger.udfps.OnScreenFingerprintUiMech"  //C15
        ).toClass().resolve().apply {
            firstMethod { name = "loadAnimDrawables" }.hook {
                if (removeMode == "3") intercept()
                else after {
                    when (removeMode) {
                        "0" -> if (isReplaceIcon) instance<Any>().setCustomDrawable(iconPath, true)
                        "1" -> instance<Any>().setCustomDrawable(null, true)
                        "2" -> {
                            instance<Any>().removePressAnim()
                            if (isReplaceIcon) instance<Any>().setCustomDrawable(iconPath, true)
                        }
                    }
                }
            }
            firstMethodOrNull { name = "startFadeInAnimation" }?.hook {
                if (isReplaceIcon) before {
                    instance<Any>().setCustomDrawable(iconPath, false)
                    resultNull()
                } else if (removeMode == "1" || removeMode == "3") intercept()
            }
            firstMethodOrNull { name = "startFadeOutAnimation" }?.hook {
                if (isReplaceIcon) intercept()
                else if (removeMode == "1" || removeMode == "3") intercept()
            }
        }
    }

    private fun Any.setCustomDrawable(iconPath: String?, update: Boolean) {
        asResolver<Any>().apply {
            val context = firstField { type = Context::class }.get<Context>() ?: return
            val drawable = if (iconPath.isNullOrBlank()) null
            else BitmapFactory.decodeFile(iconPath).toDrawable(context.resources)
            if (drawable == null) {
                firstField { name { it.contains("fadeInAnimDrawable", true) } }.set(null)
                firstField { name { it.contains("adeOutAnimDrawable", true) } }.set(null)
            }
            firstField { name { it.contains("ImMobileDrawable", true) } }.set(null)
            firstField { type = fpIconType }.get<ImageView>()?.setImageDrawable(drawable)
            if (update) firstMethod { name = "updateFpIconColor";emptyParameters() }.invoke()
        }
    }

    private fun Any.removePressAnim() {
        asResolver<Any>().firstField { name { it.contains("PressedAnimDrawable", true) } }.set(null)
        asResolver<Any>().firstField { name { it.contains("PressedAnimDrawableTmp", true) } }.set(null)
    }
}