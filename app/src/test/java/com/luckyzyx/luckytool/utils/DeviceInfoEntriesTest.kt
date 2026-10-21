package com.luckyzyx.luckytool.utils

import android.app.Application
import com.luckyzyx.luckytool.IGlobalFuncController
import com.luckyzyx.luckytool.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Proxy

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [32], application = Application::class)
class DeviceInfoEntriesTest {
    @Test
    fun `device fields preserve full values and keep hardware identifiers distinct`() {
        val fields = getDeviceInfoEntries(controller(mapOf(
            "getOtaVersion" to "RMX5200_11.C.27:1270_202610051850",
            "getSnInfo" to "SERIAL:A", "getChipInfo" to "CHIP:B", "getPcbInfo" to "BOARD:C",
        ))).associate { it.labelRes to it.value }
        assertEquals("RMX5200_11.C.27:1270_202610051850", fields[R.string.version])
        assertEquals("BOARD:C", fields[R.string.home_device_pos])
        assertEquals("SERIAL:A CHIP:B", fields[R.string.home_device_sas])
        assertFalse(fields.containsKey(R.string.home_device_pas))
    }

    @Test
    fun `unavailable service fields show a placeholder and matching identifiers are combined`() {
        val unavailable = getDeviceInfoEntries(null).associate { it.labelRes to it.value }
        assertEquals("—", unavailable[R.string.version])
        assertEquals("—", unavailable[R.string.flash])
        assertTrue(unavailable.values.none { it.contains("null") })
        val fields = getDeviceInfoEntries(controller(mapOf(
            "getSnInfo" to "SERIAL", "getChipInfo" to "SERIAL", "getPcbInfo" to "BOARD",
        ))).associate { it.labelRes to it.value }
        assertEquals("BOARD SERIAL", fields[R.string.home_device_pas])
        assertFalse(fields.containsKey(R.string.home_device_sas))
    }

    private fun controller(values: Map<String, String>): IGlobalFuncController = Proxy.newProxyInstance(
        javaClass.classLoader, arrayOf(IGlobalFuncController::class.java),
    ) { _, method, _ -> values[method.name] } as IGlobalFuncController
}
