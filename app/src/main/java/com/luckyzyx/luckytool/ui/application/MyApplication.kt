package com.luckyzyx.luckytool.ui.application

import com.google.android.material.color.DynamicColors
import android.app.Application
import com.luckyzyx.luckytool.ui.service.LxServiceBridge
import com.luckyzyx.luckytool.utils.ThemeUtils
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LxServiceBridge.init()

        applyThemeBasedOnPreferences()
    }


    private fun applyThemeBasedOnPreferences() {
        if (ThemeUtils.isDynamicColorsEnabled(this)) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }

    fun reloadAllActivities() {
        ActivityLifecycleManager.recreateAllActivities()
    }
}


























