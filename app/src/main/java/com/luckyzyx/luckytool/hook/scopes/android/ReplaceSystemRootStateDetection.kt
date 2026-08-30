package com.luckyzyx.luckytool.hook.scopes.android

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.A12
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object ReplaceSystemRootStateDetection : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode
        val isEnable = prefs(ModulePrefs).getBoolean("replace_system_root_state_detection", false)
        if (SDK < A12 || !isEnable) return

        //Source HeimdallService
        if (osCode > 26) "com.android.server.oplus.heimdall.HeimdallService".toClass().resolve().apply {
            firstMethod { name = "isRootEnable" }.hook {
                replaceToFalse()
            }
        }

        //Source RootService
        "com.android.server.oplus.heimdall.service.RootService".toClass().resolve().apply {
            firstMethod { name = "isRoot" }.hook {
                replaceToFalse()
            }
        }

        //Source HeimdallService
        "com.android.server.oplus.heimdall.root.RootDetector".toClass().resolve().apply {
            firstMethod { name = "checkDeviceRootStatus" }.hook {
                intercept()
            }
        }
    }
}