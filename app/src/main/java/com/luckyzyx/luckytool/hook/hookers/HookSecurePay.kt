package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import org.lsposed.lsparanoid.Obfuscate
import com.luckyzyx.luckytool.hook.scopes.securepay.RemoveSecurePayFoundVirusDialog
import com.luckyzyx.luckytool.utils.ModulePrefs

@Obfuscate
object HookSecurePay : Hooker {
    override fun onHook() {
        //移除支付保护发现病毒对话框
        if (prefs(ModulePrefs).getBoolean("remove_secure_pay_found_virus_dialog", false)) {
            loadHooker(RemoveSecurePayFoundVirusDialog)
        }
    }
}