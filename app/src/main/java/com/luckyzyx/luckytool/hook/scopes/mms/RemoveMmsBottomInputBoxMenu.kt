package com.luckyzyx.luckytool.hook.scopes.mms

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveMmsBottomInputBoxMenu : Hooker {
    override fun onHook() {
        //Source MenuInfoBaseBean
        "com.opos.smart.mms.interfaces.netmsg.menu.MenuInfoBaseBean".toClass().resolve().apply {
            firstMethod {
                name = "getMenus"
                returnType = List::class
            }.hook {
                replaceTo(ArrayList<Any>())
            }
        }
    }
}