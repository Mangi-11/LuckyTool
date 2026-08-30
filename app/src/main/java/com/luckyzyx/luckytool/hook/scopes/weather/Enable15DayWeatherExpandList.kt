package com.luckyzyx.luckytool.hook.scopes.weather

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hookAll
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object Enable15DayWeatherExpandList : Hooker {
    override fun onHook() {
        //Source FutureDayWeatherItem
        "com.oplus.weather.main.view.itemview.FutureDayWeatherItem".toClass().resolve().apply {
            firstFieldOrNull { name = "isAllow15DayExpand";type = Boolean::class } ?: return@apply
            constructor {
                parameters { it.contains(classOf<Boolean>()) }
            }.hookAll {
                before {
                    args(args.indexOfFirst { it is Boolean }).setTrue()
                }
            }
        }

        //Source UIConfigManager
        "com.oplus.weather.uiconfig.UIConfigManager".toClass().resolve().apply {
            optional(true).method {
                name { it.startsWith("get") && it.contains("Day15ExpandConfig") }
            }.ifEmpty {
                return@apply
            }
            method {
                name { it.startsWith("get") && it.contains("Day15ExpandConfig") }
            }.hookAll {
                replaceToTrue()
            }
        }
    }
}