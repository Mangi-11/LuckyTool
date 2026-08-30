package com.luckyzyx.luckytool.hook.scopes.ota

import android.content.Context
import android.os.PowerManager
import android.os.SystemProperties
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.result
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge
import java.io.File

@Obfuscate
class RemoveOTALocalUpdateVerity(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        loadHooker(HookABUpdateUtils(dexKitBridge))
        loadHooker(HookLocalPcakgeInfoUtil(dexKitBridge))
        loadHooker(HookPayloadProperties(dexKitBridge))
    }

    @Obfuscate
    class HookABUpdateUtils(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            //Source ABUpdateUtils
            dexKitBridge.findClass {
                matcher {
                    methods {
                        add { paramCount(0);returnType(Boolean::class.java) }
                        add { paramTypes(File::class.java, String::class.java) }
                        add { paramTypes(List::class.java) }
                    }
                    usingStrings("ABUpdateUtils")
                }
            }.apply {
                checkDataList("RemoveOTALocalUpdateVerity find ABUpdateUtils")
                single().name.toClass().resolve().apply {
                    firstMethod { parameters(File::class, String::class) }.hook {
                        after {
                            val file = args().first().cast<File>() ?: return@after
                            val list = result<java.util.ArrayList<String>>() ?: return@after

                            if (file.exists() && file.name.contains("downgrade")) {
                                list.removeIf { it.contains("forbid_ota_local_update") }
                                list.removeIf { it.contains("ota_root_or_debug") }
                            } else {
                                list.indexOfFirst { it.contains("forbid_ota_local_update") }
                                    .takeIf { it != -1 }
                                    ?.let { list[it] = "forbid_ota_local_update=false" }
                                list.indexOfFirst { it.contains("ota_root_or_debug") }
                                    .takeIf { it != -1 }
                                    ?.let { list[it] = "ota_root_or_debug=false" }
                                list.removeIf { it.contains("from_version") }
                            }
                            SystemProperties.set("sys.ota.grant_ota_local_update", "true")
                        }
                    }
                }
            }
        }
    }

    @Obfuscate
    class HookLocalPcakgeInfoUtil(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            //Source LocalPcakgeInfoUtil
            dexKitBridge.findClass {
                matcher {
                    methods {
                        add { returnType(Boolean::class.java) }
                        add {
                            paramTypes(String::class.java)
                            returnType(String::class.java)
                        }
                        add {
                            paramCount(4)
                            returnType(Int::class.java)
                        }
                    }
                    usingStrings("LocalPcakgeInfoUtil")
                }
            }.apply {
                checkDataList("RemoveOTALocalUpdateVerity find LocalPcakgeInfoUtil")
                single().name.toClass().resolve().apply {
                    firstMethod {
//                        parameters(Context::class, String::class)
                        parameters { it.contains(Context::class.java) && it.contains(String::class.java) }
                        parameterCount(2)
                        returnType { it == List::class.java || it == ArrayList::class.java }
                    }.hook {
                        after {
                            val filePath = args(args.indexOfFirst { it is String }).string()
                            val list = result<java.util.ArrayList<String>>() ?: return@after

                            if (filePath.contains("downgrade")) {
                                list.removeIf { it.contains("forbid_ota_local_update") }
                                list.removeIf { it.contains("ota_root_or_debug") }
                            } else {
                                list.indexOfFirst { it.contains("forbid_ota_local_update") }
                                    .takeIf { it != -1 }
                                    ?.let { list[it] = "forbid_ota_local_update=false" }
                                list.indexOfFirst { it.contains("ota_root_or_debug") }
                                    .takeIf { it != -1 }
                                    ?.let { list[it] = "ota_root_or_debug=false" }
                                list.removeIf { it.contains("from_version") }
                            }
                            SystemProperties.set("sys.ota.grant_ota_local_update", "true")
                        }
                    }
                }
            }
        }
    }

    @Obfuscate
    class HookPayloadProperties(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            //Source ABUpdateManager
            dexKitBridge.findClass {
                matcher {
                    fields {
//                        addForType(Context::class.java)
                        addForType("android.os.UpdateEngine")
//                        addForType("android.os.UpdateEngineCallback")
                        addForType(PowerManager.WakeLock::class.java)

                    }
                    methods {
                        add { paramCount(0);returnType(Int::class.java) }
//                        add { paramCount(0);returnType(Float::class.java) }
                        add { paramCount(0);returnType(Void.TYPE) }
                        add {
//                            paramTypes(
//                                String::class.java, Long::class.java,
//                                Long::class.java, ArrayClass(String::class)
//                            )
                            paramCount(4..5)
                            returnType(Void.TYPE)
                            usingStrings("SWITCH_SLOT_ON_REBOOT")
                        }
                    }
                    usingStrings("ABUpdateManager", "payload_properties")
                }
            }.apply {
                checkDataList("RemoveOTALocalUpdateVerity Properties")
                findMethod {
                    matcher {
                        paramCount(4..5)
                        returnType(Void.TYPE)
                        usingStrings("SWITCH_SLOT_ON_REBOOT")
                    }
                }.apply {
                    checkDataList("RemoveOTALocalUpdateVerity Properties applyPayload")
                    single().className.toClass().resolve().apply {
                        firstMethod {
                            name = single().name
                            parameterCount = single().paramCount
                            returnType(Void.TYPE)
                        }.hook {
                            before {
                                val headers = args().last().array<String>()
                                headers.toMutableList().apply {
                                    removeIf { it.contains("forbid_ota_local_update") }
                                    removeIf { it.contains("ota_root_or_debug") }
//                                removeIf { it.contains("oplus_update_engine_verify_disable") }
                                }
                                SystemProperties.set("sys.ota.grant_ota_local_update", "true")
                            }
                        }
                    }
                }
            }
        }
    }
}