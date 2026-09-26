package com.luckyzyx.luckytool.hook.utils

import android.os.IBinder
import android.os.ServiceManager
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.kavaref.extension.toClass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
@Suppress("PrivatePropertyName")
class IChargerUtils(val classLoader: ClassLoader?) {

    private val CLASS_OPLUS_CHARGER = "vendor.oplus.hardware.charger.V1_0.ICharger" //C12 C13
    private val CLASS_OPLUS_CHARGER_NEW = "vendor.oplus.hardware.charger.ICharger" //C14 C15
    private val CHARGER_STUB_CLASS = "vendor.oplus.hardware.charger.ICharger\$Stub"
    private val CHARGER_SERVICE_NAME = "vendor.oplus.hardware.charger.ICharger/default"

    val clazz = VariousClass(CLASS_OPLUS_CHARGER, CLASS_OPLUS_CHARGER_NEW)
        .load(classLoader)

    fun getInstance(): Any? {
        return if (clazz.name == CLASS_OPLUS_CHARGER) {
            clazz.resolve().firstMethod { name = "getService";emptyParameters() }.invoke()
        } else {
            val service = ServiceManager.getService(CHARGER_SERVICE_NAME)
            CHARGER_STUB_CLASS.toClass(classLoader).resolve().firstMethod {
                name = "asInterface";parameters(IBinder::class)
            }.invoke(service)
        }
    }

    fun queryChargeInfo(ins: Any?): String? {
        return clazz.resolve().firstMethod {
            name = "queryChargeInfo";emptyParameters()
        }.of(ins).invoke<String>()
    }
}