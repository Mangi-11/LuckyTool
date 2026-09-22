package com.luckyzyx.luckytool.hook.scopes.appdetail

import android.os.SystemClock
import android.view.View

/** Retries only while the host view is attached; asynchronous binding may follow its first layout. */
internal object PendingInstallerAction {
    fun watch(view: View, action: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 30_000
        var stopped = false
        lateinit var attach: View.OnAttachStateChangeListener
        val tick = object : Runnable {
            override fun run() {
                if (stopped) return
                if (SystemClock.uptimeMillis() >= deadline ||
                    (view.isAttachedToWindow && action())) {
                    stopped = true
                    view.removeOnAttachStateChangeListener(attach)
                    return
                }
                if (view.isAttachedToWindow) view.postDelayed(this, 150)
            }
        }
        attach = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) { v.removeCallbacks(tick); v.post(tick) }
            override fun onViewDetachedFromWindow(v: View) {
                stopped = true
                v.removeCallbacks(tick)
                v.removeOnAttachStateChangeListener(this)
            }
        }
        view.addOnAttachStateChangeListener(attach)
        if (view.isAttachedToWindow) view.post(tick)
    }

    fun ready(view: View) = view.isAttachedToWindow && view.isShown && view.isEnabled &&
        view.width > 0 && view.height > 0 && view.hasOnClickListeners()
}
