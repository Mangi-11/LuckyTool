package com.luckyzyx.luckytool.hook.scopes.systemui

import android.annotation.SuppressLint
import android.view.View
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.getScreenOrientation
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableNotificationAlignBothSides : YukiBaseHooker() {

    private var qsPanelPaddingPx = 0

    @SuppressLint("DiscouragedApi")
    override fun onHook() {
        //Source C12+: NotificationStackScrollLayout
        //通知卡片两侧留白由 mSidePaddings 原生控制,onMeasure 统一按 (size - mSidePaddings * 2) 测量子视图,onLayout 自动水平居中
        //锁屏媒体卡(hostView/MediaContainerView)同为 NSSL 子视图,一并覆盖
        "com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout".toClass()
            .resolve().apply {
                firstMethod { name = "onMeasure" }.hook {
                    before {
                        val layout = instance<View>()
                        if (qsPanelPaddingPx == 0) {
                            qsPanelPaddingPx = layout.resources.getDimensionPixelSize(
                                layout.resources.getIdentifier(
                                    "qs_header_panel_side_padding",
                                    "dimen",
                                    packageName
                                )
                            )
                        }
                        getScreenOrientation(layout) {
                            firstField { name = "mSidePaddings" }.of(instance)
                                .set(if (it) qsPanelPaddingPx else 0)
                        }
                    }
                }
            }
    }
}
