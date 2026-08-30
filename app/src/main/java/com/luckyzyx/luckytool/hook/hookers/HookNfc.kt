package com.luckyzyx.luckytool.hook.hookers

import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.scopes.nfc.ScanNfcTagAutoClick
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookNfc : Hooker {
    override fun onHook() {
        //扫描NFC标签自动跳转App
        loadHooker(ScanNfcTagAutoClick)
    }
}