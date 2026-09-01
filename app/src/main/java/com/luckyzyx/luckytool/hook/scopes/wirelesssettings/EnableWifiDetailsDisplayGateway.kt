package com.luckyzyx.luckytool.hook.scopes.wirelesssettings

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.wifi.WifiManager
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.toClass
import com.luckyzyx.luckytool.hook.utils.preferences.PreferenceReflections
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.formatStringAuto
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge
import java.net.Inet4Address
import java.net.Inet6Address

@Obfuscate
class EnableWifiDetailsDisplayGateway(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source WifiAddressController
        val clazz = VariousClass(
            "com.oplus.wirelesssettings.wifi.detail.WifiAddressController", //C12
            "com.oplus.wirelesssettings.wifi.detail2.WifiAddressController" //C13 C14 C15
        ).toClass()
        dexKitBridge.findClass {
            matcher {
                className(clazz.name)
            }
        }.apply {
            checkDataList("EnableWifiDetailsDisplayGateway Controller")
            findMethod {
                matcher {
                    paramCount(0)
//                    returnType(Boolean::class.java)
                    usingStrings("updateIpInfo")
                }
            }.apply {
                checkDataList("EnableWifiDetailsDisplayGateway Summary")
                single().className.toClass().resolve().apply {
                    firstMethod {
                        name = single().methodName
                        emptyParameters()
//                        returnType = Boolean::class
                    }.hook {
                        after {
                            val context = firstField { type = Context::class }.of(instance)
                                .get<Context>() ?: return@after
                            val preferenceScreen = firstField {
                                type = "androidx.preference.PreferenceScreen"
                                superclass()
                            }.of(instance).get() ?: return@after

                            val connectivityManager =
                                context.getSystemService(ConnectivityManager::class.java)
                            val wifiManager =
                                context.applicationContext.getSystemService(WifiManager::class.java)

                            val getCurrentNetwork = wifiManager.asResolver().firstMethod {
                                name = "getCurrentNetwork"
                                emptyParameters()
                                returnType = Network::class.java
                            }.invoke<Network>() ?: return@after

                            @SuppressLint("MissingPermission")
                            val linkProperties =
                                connectivityManager.getLinkProperties(getCurrentNetwork)
                                    ?: return@after

                            var ipv4Gateway = ""
                            var ipv6Gateway = ""

                            linkProperties.routes.forEachIndexed { _, routeInfo ->
                                if (routeInfo.isDefaultRoute) {
                                    val gateway = routeInfo.gateway ?: return@forEachIndexed
                                    val hostAddress = gateway.hostAddress ?: return@forEachIndexed
                                    if (routeInfo.destination.address is Inet4Address) {
                                        ipv4Gateway = hostAddress
                                    }
                                    if (routeInfo.destination.address is Inet6Address) {
                                        ipv6Gateway = hostAddress
                                    }
                                }
                            }

                            val ipv4 = PreferenceReflections.findPreference(
                                preferenceScreen, "current_ipv4_address"
                            ) ?: return@after
                            if (PreferenceReflections.isVisible(ipv4)) {
                                val summary = PreferenceReflections.getSummary(ipv4)
                                PreferenceReflections.setSummary(
                                    ipv4, formatStringAuto(
                                        arrayListOf(summary, "", ipv4Gateway), "\n"
                                    )
                                )
                            }

                            val ipv6 = PreferenceReflections.findPreference(
                                preferenceScreen, "current_ipv6_address"
                            ) ?: return@after
                            if (PreferenceReflections.isVisible(ipv6)) {
                                val summary = PreferenceReflections.getSummary(ipv6)
                                PreferenceReflections.setSummary(
                                    ipv6, formatStringAuto(
                                        arrayListOf(summary, "", ipv6Gateway), "\n"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}