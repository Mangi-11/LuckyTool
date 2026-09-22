package com.luckyzyx.luckytool.hook.scopes.appdetail

import android.app.Activity
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.TextView

/** A quiet, centered cancellation status below the native app header. */
internal object CancelInstallPage {
    private const val STATUS_TAG = "LuckyTool.CancelledStatus"

    fun simplify(activity: Activity) {
        val root = activity.window.decorView
        val resources = activity.resources
        fun id(name: String) = resources.getIdentifier(name, "id", activity.packageName)
        val processId = id("layout_process_on")
        val headerId = id("header_app_info")
        val titleId = id("process_finish_title")
        val cancelled = resources.getIdentifier("cancel_install_result_title", "string", activity.packageName)
        if (processId == 0 || headerId == 0 || titleId == 0 || cancelled == 0) return
        fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()

        // Keep the host's bound views attached but hidden. A separate TextView avoids inheriting
        // the promotional card's fixed title height, asymmetric margins and background.
        val listener = ViewTreeObserver.OnPreDrawListener {
            val process = root.findViewById<FrameLayout>(processId)
            val title = process?.findViewById<TextView>(titleId)
            var changed = false
            if (title?.text?.toString() == resources.getString(cancelled)) {
                val content = process.parent as? ViewGroup
                if (content != null) {
                    for (i in 0 until content.childCount) {
                        val child = content.getChildAt(i)
                        if (child !== process && child.id != headerId) changed = child.hide() || changed
                    }
                }
                val status = process.findViewWithTag<TextView>(STATUS_TAG) ?: TextView(activity).apply {
                    tag = STATUS_TAG
                    text = resources.getString(cancelled)
                    textSize = 18f
                    setTextColor(title.textColors)
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    gravity = Gravity.CENTER
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    includeFontPadding = false
                    minimumHeight = dp(112)
                    setPadding(dp(24), dp(32), dp(24), dp(32))
                    accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
                    process.addView(this, FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER
                    ))
                    changed = true
                }
                for (i in 0 until process.childCount) {
                    val child = process.getChildAt(i)
                    if (child !== status) changed = child.hide() || changed
                }
                if (process.background != null) process.background = null
                if (process.paddingLeft != 0 || process.paddingTop != 0 ||
                    process.paddingRight != 0 || process.paddingBottom != 0) {
                    process.setPadding(0, 0, 0, 0)
                    changed = true
                }
                if (process.layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                    process.layoutParams = process.layoutParams.apply { height = ViewGroup.LayoutParams.WRAP_CONTENT }
                    changed = true
                }
                if (status.visibility != View.VISIBLE) {
                    status.visibility = View.VISIBLE
                    changed = true
                }
            } else {
                process?.findViewWithTag<View>(STATUS_TAG)?.let { changed = it.hide() }
            }
            // Let layout settle before drawing, so old card geometry is never shown for a frame.
            !changed
        }
        root.viewTreeObserver.addOnPreDrawListener(listener)
        root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = Unit
            override fun onViewDetachedFromWindow(v: View) {
                v.viewTreeObserver.removeOnPreDrawListener(listener)
                v.removeOnAttachStateChangeListener(this)
            }
        })
    }

    private fun View.hide(): Boolean {
        if (visibility == View.GONE) return false
        visibility = View.GONE
        return true
    }
}
