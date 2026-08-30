package com.luckyzyx.luckytool.hook.scopes.launcher

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveWidgetsAddRequestWhitelist : Hooker {
    override fun onHook() {
        //Source WidgetControlHelper
        "com.android.launcher3.widget.WidgetControlHelper".toClassOrNull() ?: return
        //Source AddItemActivity
        "com.android.launcher3.dragndrop.AddItemActivity".toClass().resolve().apply {
            firstMethod {
                name = "isAllowedAddWidget"
                parameterCount { it in 1..2 }
            }.hook {
                replaceToTrue()
            }
        }
    }
}