package com.luckyzyx.luckytool.hook.scopes.settings

import android.content.Context
import android.provider.Settings
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge
import kotlin.math.max
import kotlin.math.min

@Obfuscate
class RemoveDpiRestartRecovery(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        //Source OplusDensityPreference
        "com.oplus.settings.widget.preference.OplusDensityPreference".toClass().resolve().apply {
            firstMethod {
                name = "onPreferenceChange"
                parameterCount = 2
            }.hook {
                after {
                    val newValue = args().last().string()
                    val context = firstMethod { name = "getContext";superclass() }.of(instance)
                        .invoke<Context>() ?: return@after
                    val displayMetrics = context.applicationContext.resources.displayMetrics
                    val min = min(displayMetrics.widthPixels, displayMetrics.heightPixels) *
                            160 / max(newValue.toInt(), 320)
                    val max = max(min, 120)
                    Settings.Secure.putString(
                        context.contentResolver, "display_density_forced", max.toString()
                    )
                    firstMethod { name = "notifyChanged";superclass() }.of(instance).invoke()
                }
            }
        }

        loadHooker(HookSettingsUtils(dexKitBridge))
    }

    @Obfuscate
    class HookSettingsUtils(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            //Source SettingsUtils
            dexKitBridge.findClass {
                matcher {
                    addMethod {
                        paramTypes(Context::class.java, Boolean::class.java)
                    }
                    addMethod {
                        paramTypes(
                            String::class.java,
                            Int::class.java,
                            Int::class.java,
                            Boolean::class.java
                        )
                        usingStrings("restoreCompassPhoneDisplayDensity")
                    }
                    addMethod {
                        paramTypes(Context::class.java, String::class.java, Int::class.java)
                        usingStrings("restorePhoneDisplayDensity")
                    }
                    usingStrings("SettingsUtils")
                }
            }.apply {
                checkDataList("RemoveDpiRestartRecovery Clazz")
                findMethod {
                    matcher {
                        paramTypes(Context::class.java, Boolean::class.java)
                        addInvoke {
                            paramTypes(
                                String::class.java, Int::class.java,
                                Int::class.java, Boolean::class.java
                            )
                            usingStrings("restoreCompassPhoneDisplayDensity")
                        }
                        addInvoke {
                            paramTypes(Context::class.java, String::class.java, Int::class.java)
                            usingStrings("restorePhoneDisplayDensity")
                        }
                    }
                }.apply {
                    checkDataList("RemoveDpiRestartRecovery Method")
                    single().className.toClass().resolve().apply {
                        firstMethod {
                            name = single().methodName
                            parameters(Context::class, Boolean::class)
                        }.hook {
                            intercept()
                        }
                    }
                }
            }
        }
    }
}