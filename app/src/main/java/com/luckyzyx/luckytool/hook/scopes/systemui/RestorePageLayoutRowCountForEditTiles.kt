package com.luckyzyx.luckytool.hook.scopes.systemui

import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RestorePageLayoutRowCountForEditTiles : YukiBaseHooker() {
    override fun onHook() {
        val osCode = getOSVersionCode

        //Source OplusQSCustomizer
        VariousClass(
            "com.oplusos.systemui.qs.customize.OplusQSCustomizer",  //C13
            "com.oplus.systemui.qs.customize.OplusQSCustomizer"  //C14
        ).toClass().resolve().apply {
            if (osCode < 34) {
                firstConstructor { parameterCount = 2 }.hook {
                    after {
                        firstField { name = "mMoreFunctionLabel" }.of(instance).get<View>()
                            ?.isVisible = false
                    }
                }
            }
            firstMethod { name = "updateResources" }.hook {
                after {
                    firstField { name = "mRecyclerViewTop" }.of(instance).get<View>()?.apply {
                        layoutParams = (layoutParams as LinearLayout.LayoutParams).apply {
                            height = (height / 3) * 4
                        }
                    }
                }
            }
        }
    }
}