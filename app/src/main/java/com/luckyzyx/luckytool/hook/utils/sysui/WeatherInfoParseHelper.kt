package com.luckyzyx.luckytool.hook.utils.sysui

import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.kavaref.extension.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
@Suppress("MemberVisibilityCanBePrivate")
class WeatherInfoParseHelper(val classLoader: ClassLoader?) {
    val clazz = VariousClass(
        "com.oplusos.systemui.keyguard.clock.WeatherInfoParseHelper",  //C13
        "com.oplus.systemui.keyguard.clock.WeatherInfoParseHelper"  //C14
    ).load(classLoader)

    val holderInnerClazz = "${clazz.name}\$HolderInnerClass".toClass(classLoader)
    val weatherInfoClazz = "${clazz.name}\$WeatherInfo".toClass(classLoader)
    val timeInfoClazz = "${clazz.name}\$TimeInfo".toClass(classLoader)

    fun getInstance(): Any? {
        return holderInnerClazz.resolve().firstField { type = clazz }.get()
    }

    fun getLocalTimeInfo(context: Context): Any? {
        return clazz.resolve().firstMethod {
            name = "getLocalTimeInfo"
            parameters(Context::class)
        }.of(getInstance()).invoke(context)
    }

    fun getResidentTimeInfo(context: Context, residentTimeZone: String): Any? {
        return clazz.resolve().firstMethod {
            name = "getResidentTimeInfo"
            parameters(Context::class, String::class)
        }.of(getInstance()).invoke(context, residentTimeZone)
    }
}