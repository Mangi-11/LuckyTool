package com.luckyzyx.luckytool.hook.scopes.securitypermission

import android.app.Activity
import android.content.ComponentCallbacks
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge

@Obfuscate
class EnableAlwaysAllowAppStartDialog(val dexKitBridge: DexKitBridge) : Hooker {
    override fun onHook() {
        loadHooker(HookAlwaysAllowButton(dexKitBridge))
        loadHooker(HookValidTime(dexKitBridge))
    }

    @Obfuscate
    class HookAlwaysAllowButton(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            //Source COUIAlertDialogBuilder
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(Configuration::class.java)
                        addForType(ComponentCallbacks::class.java)
                        addForType(DialogInterface.OnClickListener::class.java)
                    }
                    usingStrings("COUIAlertDialogBuilder")
                }
            }.apply {
                checkDataList("COUIAlertDialogBuilder")

                findMethod {
                    matcher {
                        paramTypes(
                            Int::class.java,
                            DialogInterface.OnClickListener::class.java,
                            Boolean::class.java
                        )
                        addUsingField {
                            type(Int::class.java)
                        }
                        usingNumbers(android.R.id.button3)
                    }
                }.apply {
                    checkDataList("setButton")

                    single().className.toClass().resolve().apply {
                        firstMethod {
                            name = single().methodName
                            parameters(
                                Int::class,
                                DialogInterface.OnClickListener::class,
                                Boolean::class
                            )
                        }.hook {
                            before {
                                val resId = args().first().int()
                                val listener = args(1).any() ?: return@before
                                val activity =
                                    listener.asResolver().firstField { type = Activity::class }
                                        .get<Activity>() ?: return@before
                                val allow30Id = activity.resources.getIdentifier(
                                    "app_start_dialog_allow_30", "string",
                                    this@HookAlwaysAllowButton.packageName
                                ).takeIf { it > 0 } ?: return@before
                                val alwaysAllowId = activity.resources.getIdentifier(
                                    "app_start_dialog_always_allow", "string",
                                    this@HookAlwaysAllowButton.packageName
                                ).takeIf { it > 0 } ?: return@before
                                if (resId == allow30Id) args().first().set(alwaysAllowId)
                            }
                        }
                    }
                }
            }
        }
    }

    @Obfuscate
    class HookValidTime(val dexKitBridge: DexKitBridge) : Hooker {
        override fun onHook() {
            //Source OplusPermissionManager
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType("android.os.ISecurityPermissionService")
                    }
                    usingStrings("OplusPermissionManager")
                }
            }.apply {
                checkDataList("OplusPermissionManager")

                findMethod {
                    matcher {
                        paramTypes(Bundle::class.java)
                        usingStrings("OplusPermissionManager", "putActivityStartWhiteList")
                    }
                }.apply {
                    checkDataList("putActivityStartWhiteList")

                    single().className.toClass().resolve().apply {
                        firstMethod {
                            name = single().methodName
                            parameters(Bundle::class)
                        }.hook {
                            before {
                                val bundle = args().first().cast<Bundle>() ?: return@before
                                bundle.remove("valid_time")
                            }
                        }
                    }
                }
            }
        }
    }
}