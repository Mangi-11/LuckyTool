package com.luckyzyx.luckytool.hook.utils.sysui

import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
@Suppress("unused")
class ClockSwitchHelper(val classLoader: ClassLoader?) {

    val clazz = VariousClass(
        "com.oplusos.systemui.keyguard.clock.ClockSwitchHelper",  //C12 C13
        "com.oplus.systemui.keyguard.clock.ClockSwitchHelper"  //C14
    ).load(classLoader)

    fun getInstance(context: Context): Any? {
        return clazz.resolve().firstMethod {
            name = "getInstance"
            parameterCount = 1
        }.invoke(context)
    }

    fun getLocatedWeatherInfo(instance: Any): Any? {
        return clazz.resolve().firstMethod {
            name = "getLocatedWeatherInfo"
            emptyParameters()
        }.of(instance).invoke()
    }

    fun getResidentWeatherInfo(instance: Any): Any? {
        return clazz.resolve().firstMethod {
            name = "getResidentWeatherInfo"
            emptyParameters()
        }.of(instance).invoke()
    }
}