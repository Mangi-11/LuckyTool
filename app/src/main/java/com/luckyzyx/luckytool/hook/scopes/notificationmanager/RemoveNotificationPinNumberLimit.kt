package com.luckyzyx.luckytool.hook.scopes.notificationmanager

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.hook.core.get
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveNotificationPinNumberLimit : Hooker {
    override fun onHook() {
        //Source AppNotificationTopController
        "com.oplus.notificationmanager.property.uicontroller.AppNotificationTopController".toClass()
            .let {
                it.resolve().apply {
                    method {
                        parameters(it, "androidx.preference.Preference", Any::class)
                        returnType = Boolean::class
                    }.hookAll {
                        before {
                            val controller = args().first().any() ?: return@before
                            val bool = args().last().boolean()
                            controller.asResolver().firstMethod { name = "onChange";superclass() }
                                .invoke(bool)
                            resultTrue()
                        }
                    }
                }
            }
    }
}