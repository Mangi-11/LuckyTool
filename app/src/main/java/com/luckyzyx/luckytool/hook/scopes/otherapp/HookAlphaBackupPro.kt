package com.luckyzyx.luckytool.hook.scopes.otherapp

import android.app.Activity
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookAlphaBackupPro : Hooker {
    override fun onHook() {
        val isPro = prefs(ModulePrefs).getBoolean("remove_check_license", false)
        if (!isPro) return
        //Source HomeActivity
        "com.ruet_cse_1503050.ragib.appbackup.pro.activities.HomeActivity".toClass().resolve().apply {
            firstMethod { name = "onCreate" }.hook {
                before {
                    instance<Activity>().intent.putExtra("licenseState", "valid_licence")
                }
            }
        }
    }
}