package com.luckyzyx.luckytool.hook.scopes.systemui

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.XLog
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.hook.core.toClass
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object FingerPrintIconAnim : Hooker {

    private const val TAG = "FpIcon"


    private val fpIconType = VariousClass(
        "com.oplusos.systemui.keyguard.onscreenfingerprint.OnScreenFingerprintIcon", //C12
        "com.oplus.systemui.keyguard.finger.onscreenfingerprint.OnScreenFingerprintIcon",  //C13
        "com.oplus.systemui.biometrics.finger.udfps.OnScreenFingerprintIcon" //C14 C15
    )

    private val animationDrawable = VariousClass(
        "com.oplus.systemui.keyguard.view.OplusAnimationDrawable" //C16
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
                    XLog.d(
                        "loadAnimDrawables after: mode=$removeMode replace=$isReplaceIcon path=$iconPath",
                        tag = TAG
                    )
                    //C16：fade 动画已无独立方法（内联为 updateOpticalUI 的 case21/22），
                    //替换图标或禁用淡入淡出时归零 fade 字段，使宿主无动画可播且不覆盖自定义图
                    if (removeMode == "1" || isReplaceIcon) instance<Any>().removeFadeAnim()
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
            //C13-C15 旧版的独立 fade 方法（C16 起不存在，仅作旧版本回退兼容）
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
            //C16：宿主恢复图标（RunnableC32041 case0/1 setImageDrawable(ImMobileDrawable)）后，
            //替换模式重设自定义图，模式1保持移除，防止恢复链抹掉我们的设置
            firstMethodOrNull { name = "restoreIconDrawable" }?.hook {
                after {
                    XLog.d("restoreIconDrawable after", tag = TAG)
                    when {
                        isReplaceIcon -> instance<Any>().setCustomDrawable(iconPath, false)
                        removeMode == "1" -> instance<Any>().setCustomDrawable(null, false)
                    }
                }
            }
            firstMethodOrNull { name = "restoreIconDrawableDark" }?.hook {
                after {
                    when {
                        isReplaceIcon -> instance<Any>().setCustomDrawable(iconPath, false)
                        removeMode == "1" -> instance<Any>().setCustomDrawable(null, false)
                    }
                }
            }
            firstMethod {
                name = "updateFpColor"
                parameters(Int::class)
            }.hook {
                after {
                    val imMobileDrawable =
                        firstField { name = "imMobileDrawable" }.of(instance).get<Drawable>()
                    imMobileDrawable?.clearColorFilter()
                    val imMobileDrawableDark =
                        firstField { name = "imMobileDrawableDark" }.of(instance).get<Drawable>()
                    imMobileDrawableDark?.clearColorFilter()
                }
            }
        }
    }

    /** 归零淡入淡出动画字段（含暗色变体与 AlphaAnimation 冻路） */
    private fun Any.removeFadeAnim() {
        asResolver().apply {
            field { type = animationDrawable.toClass() }.forEach {
                it.set(null)
            }
            firstField { name = "fadeInAlphaAnimation" }.set(null)
            firstField { name = "fadeOutAlphaAnimation" }.set(null)
        }
    }

    private fun Any.setCustomDrawable(iconPath: String?, update: Boolean) {
        asResolver().apply {
            val context = firstField { type = Context::class }.get<Context>() ?: return
            val drawable = if (iconPath.isNullOrBlank()) null
            else BitmapFactory.decodeFile(iconPath).toDrawable(context.resources)
            if (drawable == null) {
                firstField { name { it.contains("fadeInAnimDrawable", true) } }.set(null)
                firstField { name { it.contains("adeOutAnimDrawable", true) } }.set(null)
            } else {
                //C16：宿主所有的恢复/静态写点（RunnableC32041 case0/1、case21 fadeIn 链等）
                //最终都 setImageDrawable(imMobileDrawable/Dark)，把自定义图注入这些原生字段，
                //让一切恢复链写回的都是自定义图，无需逐点对抗
                firstField { name = "imMobileDrawable" }.set(drawable)
                firstFieldOrNull { name = "imMobileDrawableDark" }?.set(drawable)
                firstFieldOrNull { name = "imMobileDrawableHY" }?.set(drawable)
            }
            firstField { type = fpIconType.toClass() }.get<ImageView>()?.setImageDrawable(drawable)
            if (update) firstMethod { name = "updateFpIconColor"; emptyParameters() }.invoke()
        }
    }

    private fun Any.removePressAnim() {
        asResolver().firstField { name { it.contains("PressedAnimDrawable", true) } }.set(null)
        asResolver().firstField { name { it.contains("PressedAnimDrawableTmp", true) } }.set(null)
    }
}