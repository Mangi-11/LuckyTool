package com.luckyzyx.luckytool.hook.scopes.systemui

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.VariousClass
import com.highcapable.kavaref.extension.toClass
import com.highcapable.kavaref.extension.toClassOrNull
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.hook.core.instance
import com.luckyzyx.luckytool.hook.core.toClass
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.A15
import com.luckyzyx.luckytool.utils.SDK
import com.luckyzyx.luckytool.utils.getScreenOrientation
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object EnableNotificationAlignBothSides : Hooker {

    private var qsPanelPaddingPx = 0
    override fun onHook() {
        //Source ExpandableNotificationRow
        "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow".toClass()
            .resolve().apply {
                firstMethod { name = "onFinishInflate" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                            "ExpandableNotificationRow", method.name
                        )
                    }
                }
                firstMethod { name = "onLayout" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                            "ExpandableNotificationRow", method.name
                        )
                    }
                }
                firstMethod { name = "reInflateViews" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                            "ExpandableNotificationRow", method.name
                        )
                    }
                }
                firstMethod { name = "onConfigurationChanged" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                            "ExpandableNotificationRow", method.name
                        )
                    }
                }
                firstMethod { name = "onUiModeChanged" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                            "ExpandableNotificationRow", method.name
                        )
                    }
                }
                firstMethod { name = "onNotificationUpdated" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                            "ExpandableNotificationRow", method.name
                        )
                    }
                }
            }

        if (SDK >= A13) loadHooker(OtherNotification) else loadHooker(OtherNotificationC12)
    }

    @Obfuscate
    private object OtherNotification : Hooker {
        override fun onHook() {
            //Source KeyguardMediaController -> MediaHost -> HostView -> parent
            VariousClass(
                "com.android.systemui.media.KeyguardMediaController", //C13
                "com.android.systemui.media.controls.ui.KeyguardMediaController", //C14
                "com.android.systemui.media.controls.ui.controller.KeyguardMediaController" //C15
            ).toClass().resolve().apply {
                firstMethod { name = "setVisibility";parameterCount = 2 }.hook {
                    before {
                        if (SDK >= A15) return@before
                        val viewGroup = args().first().cast<ViewGroup>() ?: return@before
                        val visible = args().last().cast<Int>() ?: return@before
                        val count = viewGroup.childCount
                        if ((visible == 0) && (count > 0)) {
                            if (viewGroup.width != 0) viewGroup.setViewWidth(
                                "KeyguardMediaController", method.name
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
                            "UbiquitousExpandableRow", method.name
                        )
                    }
                }
                firstMethod { name = "onLayout" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                            "UbiquitousExpandableRow", method.name
                        )
                    }
                }
                firstMethod { name = "reInflateViews" }.hook {
                    after {
                        instance<ViewGroup>().setViewWidth(
                            "UbiquitousExpandableRow", method.name
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
                                ?.setViewWidth("NotificationSeedingController", method.name)
                        }
                    }
                    firstMethod { name = "onUpdate" }.hook {
                        after {
                            firstField { name = "parent" }.of(instance).get<ViewGroup>()
                                ?.setViewWidth("NotificationSeedingController", method.name)
                        }
                    }
                    firstMethod { name = "refreshNotificationPosition" }.hook {
                        after {
                            firstField { name = "parent" }.of(instance).get<ViewGroup>()
                                ?.setViewWidth("NotificationSeedingController", method.name)
                        }
                    }
                    firstMethod { name = "updateNotifSeedingViews" }.hook {
                        after {
                            firstField { name = "parent" }.of(instance).get<ViewGroup>()
                                ?.setViewWidth("NotificationSeedingController", method.name)
                        }
                    }
                }

            //Source OplusCustomRow C15
            "com.oplus.systemui.statusbar.notification.customcard.OplusCustomRow".toClassOrNull()
                ?.resolve()?.apply {
                    firstMethod { name = "onFinishInflate" }.hook {
                        after { instance<ViewGroup>().setViewWidth("OplusCustomRow", method.name) }
                    }
                    firstMethod { name = "onLayout" }.hook {
                        after { instance<ViewGroup>().setViewWidth("OplusCustomRow", method.name) }
                    }
                    firstMethod { name = "onConfigurationChanged" }.hook {
                        after { instance<ViewGroup>().setViewWidth("OplusCustomRow", method.name) }
                    }
                }
        }
    }

    @Obfuscate
    private object OtherNotificationC12 : Hooker {
        override fun onHook() {
            //Source OplusMediaHost
            "com.oplusos.systemui.media.OplusMediaHost".toClass().resolve().apply {
                firstMethod { name = "updateViewVisibility" }.hook {
                    before {
                        val hostView = firstField { name = "hostView";superclass() }.of(instance)
                            .get<ViewGroup>() ?: return@before
                        val visible = hostView.visibility
                        val count = hostView.childCount
                        if ((visible == 0) && (count > 0)) {
                            if (hostView.width != 0) hostView.setViewWidth(
                                "OplusMediaHost", method.name
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun View.setViewWidth(cls: String, methodName: String) {
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