package com.luckyzyx.luckytool.hook.scopes.launcher

import android.widget.Button
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RecentTaskListClearButton : YukiBaseHooker() {
    override fun onHook() {
        //Source OplusClearAllPanelView
        "com.oplus.quickstep.views.OplusClearAllPanelView".toClass().resolve().apply {
            (firstMethodOrNull { name = "inflateIfNeeded" }
                ?: firstMethod { name = "onFinishInflate" }).hook {
                after {
                    firstField { name = "mClearAllBtn" }.of(instance).get<Button>()?.isVisible = false
                }
            }
        }
    }
}