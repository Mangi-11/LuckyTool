package com.luckyzyx.luckytool.hook.globals

import android.content.ContentResolver
import android.database.Cursor
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.VagueType
import com.highcapable.kavaref.extension.toClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.hookAll
import com.luckyzyx.luckytool.hook.core.result
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.XLog
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class HookAppFeatureProvider(
    val dexKitBridge: DexKitBridge, private val features: Map<String, Any>
) : Hooker {

    private var isFeatureSupport = false
    private var isGetBoolean = false
    private var isGetString = false
    private var isGetInt = false

    override fun onHook() {
        if (features.isEmpty()) return

        //Source AppFeatureProviderUtils
        dexKitBridge.findClass {
            matcher {
//                addFieldForType(Uri::class.java)
//                addMethod { paramTypes(ContentResolverClass, null, String::class.java) }
                addMethod { paramTypes(ContentResolver::class.java, null) }
                addMethod {
                    usingStrings("featurename")
                    returnType(Cursor::class.java)
                }
                usingStrings(
//                    "AppFeatureProviderUtils",
                    "content://com.oplus.customize.coreapp.configmanager.configprovider.AppFeatureProvider"
                )
            }
        }.apply {
            checkDataList("AppFeatureProviderUtils [$packageName]")
            findMethod {
                matcher {
//                    name("isFeatureSupport")
                    paramTypes(ContentResolver::class.java, String::class.java)
                    returnType(Boolean::class.java)
                }
            }.apply {
                if (!isFeatureSupport) isFeatureSupport = singleOrNull() != null
                singleOrNull()?.let {
                    it.className.toClass().resolve().apply {
                        firstMethod {
                            name = single().methodName
                            parameters(ContentResolver::class, String::class)
                            returnType = Boolean::class
                        }.hook {
                            before {
                                val key = args().last().cast<String>()
                                if (key.isNullOrBlank()) return@before
                                val value = features[key]
                                if (value != null && value is Boolean) result = value
                            }
                        }
                    }
                }
            }
            findMethod {
                matcher {
//                    name("isFeatureSupport")
                    paramTypes(ContentResolver::class.java, null, String::class.java)
                    returnType(Boolean::class.java)
                }
            }.apply {
                if (!isFeatureSupport) isFeatureSupport = singleOrNull() != null
                singleOrNull()?.let {
                    it.className.toClass().resolve().apply {
                        firstMethod {
                            name = single().methodName
                            parameters(ContentResolver::class, VagueType, String::class)
                            returnType = Boolean::class
                        }.hook {
                            before {
                                val key = args().last().cast<String>()
                                if (key.isNullOrBlank()) return@before
                                val value = features[key]
                                if (value != null && value is Boolean) result = value
                            }
                        }
                    }
                }
            }
            if (!isFeatureSupport) {
                XLog.debug("AppFeatureProviderUtils [$packageName] -> isFeatureSupport is null")
            }

            findMethod {
                matcher {
//                    name("getBoolean")
                    paramTypes(ContentResolver::class.java, String::class.java, Boolean::class.java)
                    returnType(Boolean::class.java)
                }
            }.apply {
                isGetBoolean = singleOrNull() != null
                singleOrNull()?.let {
                    it.className.toClass().resolve().apply {
                        firstMethod {
                            name = single().methodName
                            parameters(
                                ContentResolver::class,
                                String::class,
                                Boolean::class
                            )
                            returnType = Boolean::class
                        }.hook {
                            before {
                                val key = args(1).cast<String>()
                                if (key.isNullOrBlank()) return@before
                                val value = features[key]
                                if (value != null && value is Boolean) result = value
                            }
                        }
                    }
                }
            }

            findMethod {
                matcher {
//                    name("getString")
                    paramTypes(ContentResolver::class.java, String::class.java, String::class.java)
                    returnType(String::class.java)
                }
            }.apply {
                isGetString = singleOrNull() != null
                singleOrNull()?.let {
                    it.className.toClass().resolve().apply {
                        firstMethod {
                            name = single().methodName
                            parameters(
                                ContentResolver::class,
                                String::class,
                                String::class
                            )
                            returnType = String::class
                        }.hook {
                            before {
                                val key = args(1).cast<String>()
                                if (key.isNullOrBlank()) return@before
                                val value = features[key]
                                if (value != null && value is String) result = value
                            }
                        }
                    }
                }
            }

            findMethod {
                matcher {
//                    name("getInt")
                    paramTypes(ContentResolver::class.java, String::class.java, Int::class.java)
                    returnType(Int::class.java)
                }
            }.apply {
                isGetInt = singleOrNull() != null
                singleOrNull()?.let {
                    it.className.toClass().resolve().apply {
                        firstMethod {
                            name = single().methodName
                            parameters(
                                ContentResolver::class,
                                String::class,
                                Int::class
                            )
                            returnType = Int::class
                        }.hook {
                            before {
                                val key = args(1).cast<String>()
                                if (key.isNullOrBlank()) return@before
                                val value = features[key]
                                if (value != null && value is Int) result = value
                            }
                        }
                    }
                }
            }
        }
    }
}