package com.luckyzyx.luckytool.hook.hookers

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.hook.scopes.securepay.RemoveSecurePayFoundVirusDialog
import com.luckyzyx.luckytool.utils.ModulePrefs
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookSecurePay : YukiBaseHooker() {
    override fun onHook() {
        //移除支付保护发现病毒对话框
        if (preferences(ModulePrefs).getBoolean("remove_secure_pay_found_virus_dialog", false)) {
            loadHooker(RemoveSecurePayFoundVirusDialog)
        }
    }
}