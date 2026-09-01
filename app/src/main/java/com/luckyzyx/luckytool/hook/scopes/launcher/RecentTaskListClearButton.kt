package com.luckyzyx.luckytool.hook.scopes.launcher

import android.widget.Button
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RecentTaskListClearButton : Hooker {
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