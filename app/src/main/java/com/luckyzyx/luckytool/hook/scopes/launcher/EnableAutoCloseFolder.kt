package com.luckyzyx.luckytool.hook.scopes.launcher

import android.view.ViewGroup
import androidx.core.view.children
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.isNotSubclassOf
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableAutoCloseFolder : Hooker {
    override fun onHook() {
        //Source AbstractFloatingView
        "com.android.launcher3.AbstractFloatingView".toClass().let {
            it.resolve().apply {
                firstMethod {
                    name = "closeOpenViews"
                    parameterCount = 4
                }.hook {
                    before {
                        val activityContext = args().first().any() ?: return@before
                        val animate = args(1).boolean()
                        val type = args(2).int()

                        val typeFolder =
                            firstField { name = "TYPE_FOLDER" }.get<Int>() ?: return@before
                        if ((type and typeFolder) == 0) return@before

                        val dragLayer = activityContext.asResolver().firstMethod {
                            name = "getDragLayer";superclass()
                        }.invoke<ViewGroup>() ?: return@before
                        dragLayer.children.forEachIndexed { _, view ->
                            if (view::class isNotSubclassOf it) return@forEachIndexed
                            val isOfType = view.asResolver().firstMethod {
                                name = "isOfType";superclass()
                            }.invoke<Boolean>(typeFolder) ?: false
                            if (isOfType) {
                                view.asResolver().firstMethod {
                                    name = "close";parameterCount = 1;superclass()
                                }.invoke(animate)
                            }
                        }
                    }
                }
            }
        }

    }
}