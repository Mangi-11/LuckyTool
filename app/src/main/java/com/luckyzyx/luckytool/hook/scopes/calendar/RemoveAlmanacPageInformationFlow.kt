package com.luckyzyx.luckytool.hook.scopes.calendar

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveAlmanacPageInformationFlow : Hooker {
    override fun onHook() {
        //Source AlmanacPagesAdapter -> H5InterfaceHelper getAlmanacUrl
        "com.android.calendar.module.subscription.almanac.adapter.AlmanacPagesAdapter".toClass()
            .resolve().apply {
                firstMethod { name = "onCreateViewHolder" }.hook {
                    before {
                        args().last().set(0)
                    }
                }
            }
    }
}