package com.luckyzyx.luckytool.hook.hookers

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.hook.scopes.nfc.ScanNfcTagAutoClick
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookNfc : YukiBaseHooker() {
    override fun onHook() {
        //扫描NFC标签自动跳转App
        loadHooker(ScanNfcTagAutoClick)
    }
}