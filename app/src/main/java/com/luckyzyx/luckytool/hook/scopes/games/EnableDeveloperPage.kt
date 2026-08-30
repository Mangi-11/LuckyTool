package com.luckyzyx.luckytool.hook.scopes.games

import android.app.Activity
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableDeveloperPage : Hooker {
    override fun onHook() {
        //Source GameDevelopOptionsActivity
        "business.compact.activity.GameDevelopOptionsActivity".toClass().resolve().apply {
            firstMethod {
                name = "onCreate"
                parameterCount = 1
            }.hook {
                before {
                    instance<Activity>().intent.apply {
                        putExtra("gameDevelopOptions", "GameDevelopOptionsActivity")
                        putExtra("openAutomation", -1)
                    }
                    args().first().setNull()
                }
            }
        }
    }
}