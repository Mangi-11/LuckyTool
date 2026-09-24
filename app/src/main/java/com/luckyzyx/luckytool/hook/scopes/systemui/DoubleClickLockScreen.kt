package com.luckyzyx.luckytool.hook.scopes.systemui

import android.view.MotionEvent
import android.view.ViewGroup
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.closeScreen
import org.lsposed.lsparanoid.Obfuscate
import kotlin.math.abs

@Obfuscate
object DoubleClickLockScreen : YukiBaseHooker() {
    override fun onHook() {
        var curTouchTime = 0L
        var curTouchX = 0F
        var curTouchY = 0F

        //Source PhoneStatusBarView
        "com.android.systemui.statusbar.phone.PhoneStatusBarView".toClass().resolve().apply {
            firstMethod { name = "onFinishInflate" }.hook {
                before {
                    val statusbar = instance<ViewGroup>()
                    statusbar.setOnTouchListener { v, event ->
                        if (event.action != MotionEvent.ACTION_DOWN) return@setOnTouchListener false
                        val lastTouchTime = curTouchTime
                        val lastTouchX = curTouchX
                        val lastTouchY = curTouchY
                        curTouchTime = System.currentTimeMillis()
                        curTouchX = event.x
                        curTouchY = event.y
                        if (curTouchTime - lastTouchTime < 250L && abs(curTouchX - lastTouchX) < 100f && abs(
                                curTouchY - lastTouchY
                            ) < 100f
                        ) {
                            closeScreen(v.context)
                            curTouchTime = 0L
                            curTouchX = 0F
                            curTouchY = 0F
                        }
                        v.performClick()
                        false
                    }
                }
            }
        }
    }
}