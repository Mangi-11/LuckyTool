package com.luckyzyx.luckytool.hook.scopes.ota

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.SystemProperties
import android.view.Menu
import androidx.core.content.edit
import com.highcapable.betterandroid.ui.extension.view.toast
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.XLog
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.utils.DexkitUtils.checkDataList
import com.luckyzyx.luckytool.utils.FileUtils
import com.luckyzyx.luckytool.utils.showToast
import org.lsposed.lsparanoid.Obfuscate
import org.luckypray.dexkit.DexKitBridge
import java.io.File
import java.lang.reflect.Modifier

@Obfuscate
class EnableOpexLocalInstall(val dexKitBridge: DexKitBridge) : Hooker {

    val packageListInfo = "com.oplus.ota.db.PackageListInfo"

    val opexCopyResultCode = "com.oplus.ota.opex.OpexPackageHelper\$OpexCopyResultCode"

    val OpexMenuItemCode = 10000

    override fun onHook() {
        //OpexPackageHelper
        //"com.oplus.ota.opex.OpexPackageHelper"
        val opexPackageHelper = dexKitBridge.findClass {
            matcher {
                addMethod {
                    paramTypes(String::class.java)
                    returnType(packageListInfo)
                }
                usingStrings("OpexPackageHelper")
            }
        }.apply {
            checkDataList("OpexPackageHelper")
        }.single().name
        val helper = opexPackageHelper.toClass()
        val copyMethod = helper.declaredMethods.singleOrNull { method ->
            val params = method.parameterTypes
            Modifier.isStatic(method.modifiers) &&
                method.returnType.name == opexCopyResultCode &&
                params.size in 3..4 &&
                params[0] == Context::class.java &&
                params[1].name == packageListInfo &&
                params[2] == Int::class.javaPrimitiveType &&
                (params.size == 3 || params[3] == Boolean::class.javaPrimitiveType)
        } ?: error("OpexPackageHelper copy method not found")
        copyMethod.isAccessible = true

        //Source EntryActivity
        "com.oplus.otaui.activity.EntryActivity".toClass().resolve().apply {
            firstMethod {
                name = "onCreateOptionsMenu"
                parameters(Menu::class)
                returnType = Boolean::class
            }.hook {
                after {
                    val activity = instance<Activity>()
                    val menu = args().first().cast<Menu>() ?: return@after
                    menu.add(0, OpexMenuItemCode, 0, "Opex")
                    menu.findItem(OpexMenuItemCode)?.setOnMenuItemClickListener {
                        val intent = Intent("android.intent.action.OPEN_DOCUMENT")
                        intent.addCategory("android.intent.category.OPENABLE")
                        intent.setType("*/*")
                        activity.startActivityForResult(intent, OpexMenuItemCode)
                        true
                    }

                }
            }
            firstMethod {
                name = "onActivityResult"
                parameters(Int::class, Int::class, Intent::class)
                returnType = Void.TYPE
            }.hook {
                before {
                    val activity = instance<Activity>()
                    val requestCode = args().first().int()
                    val resultCode = args(1).int()
                    val intent = args().last().cast<Intent>() ?: return@before
                    if (requestCode == OpexMenuItemCode && resultCode == Activity.RESULT_OK) {
                        try {
                            val sp =
                                activity.getSharedPreferences("state_info", Context.MODE_PRIVATE)
                            sp.edit(commit = true) {
                                putString(
                                    "realOtaVersion",
                                    SystemProperties.get("ro.build.version.ota", "")
                                )
                            }
                        } catch (t: Throwable) {
                            XLog.debug("prefs state_info error: ${t.message}")
                        }

                        val uri = intent.data ?: return@before
                        try {
                            activity.contentResolver.takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (t: Throwable) {
                            XLog.debug("takePersistableUriPermission error: ${t.message}")
                        }

                        val name = uri.path?.substringAfterLast("/") ?: return@before
                        if (!name.startsWith("ovl_update")) {
                            activity.toast("not ovl_update")
                            return@before
                        }

                        val opexDir = File(activity.cacheDir, "opexs_cache")
                        if (opexDir.exists()) FileUtils.deleteFile(opexDir)
                        if (!opexDir.exists()) opexDir.mkdirs()

                        val opexFile = File(opexDir, name)
                        if (!opexFile.exists()) opexFile.createNewFile()
                        FileUtils.copyUriToFile(activity, uri, opexFile)

                        val fileSize = opexDir.listFiles {
                            it.name.startsWith("ovl_update")
                        } ?: arrayOf()

                        val info = helper.resolve().firstMethod {
                            parameters(String::class)
                            returnType = packageListInfo
                        }.invoke(opexDir.path) ?: return@before

                        fileSize.forEachIndexed { index, file ->
                            val name = file.nameWithoutExtension.substringAfterLast("/")
                            val code = if (copyMethod.parameterCount == 4) {
                                copyMethod.invoke(null, activity, info, index, false)
                            } else {
                                copyMethod.invoke(null, activity, info, index)
                            }
                            XLog.debug("$name -> $code")
                            activity.showToast("$name -> $code")
                        }

                        FileUtils.deleteFile(opexDir)
                        resultNull()
                    }
                }
            }
        }
    }
}
