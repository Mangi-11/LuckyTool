package com.luckyzyx.luckytool.hook.scopes.externalstorage

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveStorageLimit : Hooker {
    override fun onHook() {
        //Source ExternalStorageProvider
        "com.android.externalstorage.ExternalStorageProvider".toClass().resolve().apply {
            firstMethodOrNull { name = "shouldBlockDirectoryFromTree" }?.hook {
                replaceToFalse()
            }
        }
    }
}