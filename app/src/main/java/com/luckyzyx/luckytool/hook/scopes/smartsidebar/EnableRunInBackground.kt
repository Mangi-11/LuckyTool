package com.luckyzyx.luckytool.hook.scopes.smartsidebar

import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.injectModuleResources
import com.luckyzyx.luckytool.R
import com.luckyzyx.luckytool.utils.IntentUtils
import com.luckyzyx.luckytool.utils.getOSVersionCode
import com.luckyzyx.luckytool.utils.startMirageWindow
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableRunInBackground : YukiBaseHooker() {

    override fun onHook() {
        val osCode = getOSVersionCode

        val targetTool = VariousClass(
            "com.oplus.smartsidebar.panelview.edgepanel.data.entrybeans.models.tools.BackgroundRunTool", //C15-
            "com.oplus.smartsidebar.panelview.edgepanel.data.entrybeans.models.tools.GTModelTool", //C16
            "com.oplus.smartsidebar.panelview.edgepanel.data.entrybeans.models.tools.CleanStorageTool" //C16.1
        ).toClass()

        //Source BackgroundRunTool or GTModelTool
        targetTool.resolve().apply {
            if (targetTool.simpleName != "BackgroundRunTool") {
                var context: Context?
                firstConstructor { parameters(Context::class) }.hook {
                    before {
                        context = firstArg().get<Context>() ?: return@before
                        context.injectModuleResources()
                    }
                }
                firstMethod { name = "getIconRes" }.hook {
                    intercept(R.drawable.background_run)
                }
                firstMethod { name = "getNameRes" }.hook {
                    intercept(R.string.run_in_background)
                }
            }
            firstMethod { name = "handle" }.hook {
                before {
                    if (osCode >= 34) {
                        startMirageWindow(null)
                    } else {
                        val context = firstField { type = Context::class; superclass() }
                            .of(instance).get<Context>() ?: return@before
                        IntentUtils(context).startBackgroundRunServiceV14()
                    }
                    result = null
                }
            }
            firstMethod { name = "isToolAvailable" }.hook {
                intercept(true)
            }
        }

        //Source ToolEntryHelper
        "com.oplus.smartsidebar.panelview.edgepanel.data.entrybeans.ToolEntryHelper".toClass()
            .resolve().apply {
                firstMethod { name = "loadTools" }.hook {
                    after {
                        val context =
                            firstField { type = Context::class; superclass() }.of(instance)
                                .get<Context>() ?: return@after
                        val tool = targetTool.createInstance(context, isPublic = false)
                        firstMethod { name = "put" }.of(instance).invoke(tool)
                    }
                }
            }

        //Source ImageDataHandleImpl
        "com.oplus.smartsidebar.panelview.edgepanel.data.viewdatahandlers.ImageDataHandleImpl".toClass()
            .resolve().apply {
                firstMethod { name = "getToolAppIcon" }.hook {
                    before {
                        "com.coloros.common.App".toClass().resolve().firstField {
//                            name = "sContext"
                            modifiers(Modifiers.STATIC)
                            type = Context::class
                        }.get<Context>()?.injectModuleResources()
                    }
                }
            }
    }
}