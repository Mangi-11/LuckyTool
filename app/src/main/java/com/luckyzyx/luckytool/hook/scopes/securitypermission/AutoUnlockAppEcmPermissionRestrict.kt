package com.luckyzyx.luckytool.hook.scopes.securitypermission

import android.app.Activity
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.utils.EcmUtils
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object AutoUnlockAppEcmPermissionRestrict : Hooker {
    override fun onHook() {
        //Source PermissionGroupsActivity
        "com.oplusos.securitypermission.permission.PermissionGroupsActivity".toClass().resolve()
            .apply {
                firstMethod { name = "onCreate" }.hook {
                    before {
                        val activity = instance<Activity>()
                        val packName = activity.intent.getStringExtra("packageName")
                            ?: activity.intent.getStringExtra("mPackageName")
                            ?: return@before
                        EcmUtils(activity).autoUnlockRestrictedSettings(packName)
                    }
                }
            }
    }
}