package com.luckyzyx.luckytool.hook.scopes.settings

import android.content.Context
import android.content.Intent
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.EcmUtils
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class AutoUnlockRestrictedSettings(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode

        if (osCode >= 34) loadHooker(RestrictedSettings)
        else loadHooker(RestrictedSettingsV14(dexKitBridge))
    }

    @Obfuscate
    object RestrictedSettings : Hooker {
        override fun onHook() {
            //Source RestrictedPreferenceHelper
            "com.oplus.settings.widget.preference.RestrictedPreferenceHelper".toClass().resolve()
                .apply {
                    firstMethod {
                        name = "performClick"
                        emptyParameters()
                        returnType = Boolean::class
                    }.hook {
                        before {
                            val context = firstField { type = Context::class }.of(instance)
                                .get<Context>() ?: return@before
                            val intent = firstField { type = Intent::class }.of(instance)
                                .get<Intent>() ?: return@before
                            val packName =
                                intent.getStringExtra("android.intent.extra.PACKAGE_NAME")
                                    ?: return@before
//                        val permissionName = intent.getStringExtra("android.intent.extra.SUBJECT")
//                            ?: return@before
//                            val uid = intent.getIntExtra("android.intent.extra.UID", -1)
                            EcmUtils(context).autoUnlockRestrictedSettings(packName)
                            firstMethod {
                                name = "setDisabledByEcm"
                                parameters(Intent::class)
                                returnType = Boolean::class
                            }.of(instance).invoke(null)
                            resultFalse()
                        }
                    }
                }
        }
    }

    @Obfuscate
    class RestrictedSettingsV14(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            val limit = false
            //Source RestrictedPreferenceHelper
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(Context::class.java)
                        addForType(String::class.java)
                        addForType(Boolean::class.java)
                        addForType(Int::class.java)
                    }
                    methods {
                        add {
                            paramCount(0)
                            returnType(Boolean::class.java)
                        }
                        add {
                            paramCount(0)
                            returnType(Void.TYPE)
                        }
                        add {
                            paramTypes(Boolean::class.java)
                            returnType(Boolean::class.java)
                        }
                    }
                    usingStrings("RestrictedPreferenceHelper")
                }
            }.apply {
                checkDataList("AutoUnlockRestrictedSettings findClass")
                val findMethod = findMethod {
                    matcher {
                        paramCount(0)
                        returnType(Boolean::class.java)
                        addCaller {
                            name("performClick")
                            returnType(Void.TYPE)
                        }
                        addUsingField {
                            field { type(Context::class.java) }
                            field { type(Int::class.java) }
                            field { type(String::class.java) }
                            field { type(Boolean::class.java) }
                        }
                    }
                }.checkDataList("AutoUnlockRestrictedSettings findMethod").single()

                val fields = findField {
                    matcher {
                        addReadMethod {
                            name(findMethod.methodName)
                            paramCount(0)
                            returnType(Boolean::class.java)
                        }
                    }
                }.checkDataList("AutoUnlockRestrictedSettings findFields", onlyOne = false)

                val appops = findField {
                    matcher {
                        type(Boolean::class.java)
                        addReadMethod {
                            name(findMethod.methodName)
                            paramCount(0)
                            returnType(Boolean::class.java)
                        }
                        addWriteMethod {
                            paramTypes(Boolean::class.java.name)
                            returnType(Boolean::class.java)
                        }
                    }
                }.checkDataList("AutoUnlockRestrictedSettings findField AppOps").single()

                val admin =
                    fields.filter { it.typeName == Boolean::class.java.name }.toMutableList()
                        .apply {
                            removeIf { it.fieldName == appops.fieldName }
                        }.first()

//                val uidname =
//                    fields.find { it.typeName == Int::class.java.name }?.fieldName ?: "uid"
                val packname = fields.find { it.typeName == String::class.java.name }?.fieldName
                    ?: "packageName"

                findMethod.className.toClass().resolve().apply {
                    firstMethod {
                        name = findMethod.methodName
                        emptyParameters()
                        returnType = Boolean::class
                    }.hook {
                        before {
                            val context =
                                firstField { type = Context::class }.of(instance).get<Context>()
                                    ?: return@before
                            val disabledAdmin =
                                firstField { name = admin.fieldName }.of(instance).get<Boolean>()
                                    ?: false
                            val disabledAppOps = firstField { name = appops.fieldName }.of(instance)
                                .get<Boolean>() ?: false
                            if (disabledAdmin) return@before
                            if (disabledAppOps) {
//                                val uid =
//                                    firstField { name = uidname }.of(instance).get<Int>() ?: -1
                                val packName =
                                    firstField { name = packname }.of(instance).get<String>() ?: ""
                                EcmUtils(context).autoUnlockRestrictedSettings(packName)
                                firstMethod {
                                    parameters(Boolean::class)
                                    returnType = Boolean::class
                                }.of(instance).invoke(limit)
                                result = limit
                            }
                        }
                    }
                }
            }
        }
    }
}