package com.luckyzyx.luckytool.hook.scopes.appdetail

import android.app.Activity
import android.view.View
import android.view.ViewTreeObserver

/** Removes the local store promotion shown when the server returns no recommendations. */
internal object InstallFinishPage {
    fun hideStorePromotion(activity: Activity) {
        val root = activity.window.decorView
        val cardId = activity.resources.getIdentifier("install_done_suggest_B", "id", activity.packageName)
        if (cardId == 0) return
        // The completion Fragment is attached asynchronously, and LiveData can show this card again.
        val listener = ViewTreeObserver.OnPreDrawListener {
            val card = root.findViewById<View>(cardId)
            if (card != null && card.visibility != View.GONE) {
                card.visibility = View.GONE
                false
            } else true
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
}
