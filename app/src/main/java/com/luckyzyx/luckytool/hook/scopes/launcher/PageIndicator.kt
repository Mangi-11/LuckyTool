package com.luckyzyx.luckytool.hook.scopes.launcher

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import com.luckyzyx.luckytool.utils.safeOfNull
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object PageIndicator : Hooker {
    override fun onHook() {
        val removeDesktop = prefs(ModulePrefs).getBoolean("remove_pagination_component", false)
        val removeFolder =
            prefs(ModulePrefs).getBoolean("remove_folder_pagination_component", false)
        val disableSliding =
            prefs(ModulePrefs).getBoolean("disable_pagination_component_sliding", false)

        //Source OplusPageIndicator
        "com.android.launcher.pageindicators.OplusPageIndicator".toClass().resolve().apply {
            (firstMethodOrNull { name = "dispatchDraw"; parameters(Canvas::class) }
                ?: firstMethod { name = "onDraw"; parameters(Canvas::class) }).hook {
                before {
                    val view = instance<View>()
                    val parentView = if (view.parent != null) view.parent as View else return@before
                    val entryName = safeOfNull {
                        view.resources.getResourceEntryName(parentView.id)
                    } ?: return@before
                    when (entryName) {
                        "drag_layer" -> if (removeDesktop) {
                            view.isVisible = false
                            resultNull()
                        }

                        "folder_content_root" -> if (removeFolder) {
                            view.isVisible = false
                            resultNull()
                        }
                    }
                }
            }
        }

        if (SDK < A13) return

        //Source PageIndicatorTouchHelper
        "com.android.launcher.pageindicators.PageIndicatorTouchHelper".toClass().resolve().apply {
            firstMethod {
                name = "onActionMove"
                parameters(MotionEvent::class)
            }.hook {
                if (disableSliding) intercept()
            }
        }

        //Source BigFolderIcon
        "com.android.launcher3.folder.big.BigFolderIcon".toClassOrNull()?.resolve()?.apply {
            firstMethod { name = "onScrollPageStart" }.hook {
                after {
                    if (removeFolder) firstField { name = "indicator" }.of(instance)
                        .get<View>()?.isVisible = false
                }
            }
            firstMethod { name = "exposureForWorkspace" }.hook {
                after {
                    if (removeFolder) firstField { name = "indicator" }.of(instance)
                        .get<View>()?.isVisible = false
                }
            }
        }
    }
}