package com.luckyzyx.luckytool.hook.scopes.systemui

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.A15
import com.luckyzyx.luckytool.utils.SDK
import com.luckyzyx.luckytool.utils.getScreenOrientation
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableNotificationAlignBothSides : YukiBaseHooker() {

    private var qsPanelPaddingPx = 0
    override fun onHook() {
        //Source ExpandableNotificationRow
        "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow".toClass()
            .resolve().apply {
                firstMethod { name = "onFinishInflate" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                        )
                    }
                }
                firstMethod { name = "onLayout" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                        )
                    }
                }
                firstMethod { name = "reInflateViews" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                        )
                    }
                }
                firstMethod { name = "onConfigurationChanged" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                        )
                    }
                }
                firstMethod { name = "onUiModeChanged" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                        )
                    }
                }
                firstMethod { name = "onNotificationUpdated" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                        )
                    }
                }
            }

        if (SDK >= A13) loadHooker(OtherNotification) else loadHooker(OtherNotificationC12)
    }

    @Obfuscate
    private object OtherNotification : YukiBaseHooker() {
        override fun onHook() {
            //Source KeyguardMediaController -> MediaHost -> HostView -> parent
            VariousClass(
                "com.android.systemui.media.KeyguardMediaController", //C13
                "com.android.systemui.media.controls.ui.KeyguardMediaController", //C14
                "com.android.systemui.media.controls.ui.controller.KeyguardMediaController" //C15
            ).toClass().resolve().apply {
                firstMethod { name = "setVisibility"; parameterCount = 2 }.hook {
                    before {
                        if (SDK >= A15) return@before
                        val viewGroup = firstArg().get<ViewGroup>() ?: return@before
                        val visible = lastArg().get<Int>() ?: return@before
                        val count = viewGroup.childCount
                        if ((visible == 0) && (count > 0)) {
                            if (viewGroup.width != 0) viewGroup.setViewWidth(
                            )
                        }
                    }
                }
            }

            //Source UbiquitousExpandableRow
            VariousClass(
                "com.oplusos.systemui.statusbar.notification.row.UbiquitousExpandableRow", //C13
                "com.oplus.systemui.statusbar.notification.row.UbiquitousExpandableRow" //C14 or null
            ).loadOrNull()?.resolve()?.apply {
                firstMethod { name = "onFinishInflate" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                        )
                    }
                }
                firstMethod { name = "onLayout" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                        )
                    }
                }
                firstMethod { name = "reInflateViews" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                        )
                    }
                }
            }

            //Source NotificationSeedingController C14
            "com.oplus.systemui.plugins.seedling.notification.NotificationSeedingController".toClassOrNull()
                ?.resolve()?.apply {
                    firstMethod { name = "onCreateView" }.hook {
                        after {
                            firstField { name = "parent" }.of(instance).get<ViewGroup>()
                                ?.setViewWidth()
                        }
                    }
                    firstMethod { name = "onUpdate" }.hook {
                        after {
                            firstField { name = "parent" }.of(instance).get<ViewGroup>()
                                ?.setViewWidth()
                        }
                    }
                    firstMethod { name = "refreshNotificationPosition" }.hook {
                        after {
                            firstField { name = "parent" }.of(instance).get<ViewGroup>()
                                ?.setViewWidth()
                        }
                    }
                    firstMethod { name = "updateNotifSeedingViews" }.hook {
                        after {
                            firstField { name = "parent" }.of(instance).get<ViewGroup>()
                                ?.setViewWidth()
                        }
                    }
                }

            //Source OplusCustomRow C15
            "com.oplus.systemui.statusbar.notification.customcard.OplusCustomRow".toClassOrNull()
                ?.resolve()?.apply {
                    firstMethod { name = "onFinishInflate" }.hook {
                        after { instance<ViewGroup>().setViewWidth() }
                    }
                    firstMethod { name = "onLayout" }.hook {
                        after { instance<ViewGroup>().setViewWidth() }
                    }
                    firstMethod { name = "onConfigurationChanged" }.hook {
                        after { instance<ViewGroup>().setViewWidth() }
                    }
                }
        }
    }

    @Obfuscate
    private object OtherNotificationC12 : YukiBaseHooker() {
        override fun onHook() {
            //Source OplusMediaHost
            "com.oplusos.systemui.media.OplusMediaHost".toClass().resolve().apply {
                firstMethod { name = "updateViewVisibility" }.hook {
                    before {
                        val hostView = firstField { name = "hostView"; superclass() }.of(instance)
                            .get<ViewGroup>() ?: return@before
                        val visible = hostView.visibility
                        val count = hostView.childCount
                        if ((visible == 0) && (count > 0)) {
                            if (hostView.width != 0) hostView.setViewWidth(
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun View.setViewWidth() {
        qsPanelPaddingPx = resources.getDimensionPixelSize(
            resources.getIdentifier("qs_header_panel_side_padding", "dimen", packageName)
        )
        val targetWidth = resources.displayMetrics.widthPixels - (qsPanelPaddingPx * 2)

        getScreenOrientation(this) {
            if (layoutParams != null) when (layoutParams) {
                is FrameLayout.LayoutParams -> {
                    layoutParams = FrameLayout.LayoutParams(layoutParams).apply {
                        width = if (it) targetWidth else FrameLayout.LayoutParams.MATCH_PARENT
                        gravity = Gravity.CENTER_HORIZONTAL
                    }
                }

                else -> {
                    layoutParams = ViewGroup.LayoutParams(layoutParams).apply {
                        width = if (it) targetWidth else ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
            }
        }
    }
}