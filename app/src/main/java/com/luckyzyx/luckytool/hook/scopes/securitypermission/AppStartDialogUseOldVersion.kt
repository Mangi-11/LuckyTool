package com.luckyzyx.luckytool.hook.scopes.securitypermission

import android.app.Activity
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object AppStartDialogUseOldVersion : Hooker {
    override fun onHook() {
        //Source AppStartConfirmDialogActivity
        "com.oplusos.securitypermission.permission.ui.AppStartConfirmDialogActivity".toClass()
            .resolve().apply {
                firstMethod { name = "onCreate" }.hook {
                    before {
                        val activity = instance<Activity>()
                        activity.intent.putExtra("activity_start_confirm_version", 0)
                    }
                }
            }
    }
}