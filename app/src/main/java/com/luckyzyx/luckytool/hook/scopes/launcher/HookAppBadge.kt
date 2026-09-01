package com.luckyzyx.luckytool.hook.scopes.launcher

import android.graphics.drawable.Drawable
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object HookAppBadge : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode
        if (osCode >= 30) loadHooker(AppBadge) else loadHooker(AppBadgeC13)
    }

    @Obfuscate
    object AppBadge : Hooker {
        override fun onHook() {
            val isShortcut = prefs(ModulePrefs).getBoolean("remove_app_shortcut_badge", false)
            val isWork = prefs(ModulePrefs).getBoolean("remove_app_work_badge", false)
            val isClone = prefs(ModulePrefs).getBoolean("remove_app_clone_badge", false)

            //Source BitmapInfo
            "com.android.launcher3.icons.BitmapInfo".toClass().resolve().apply {
                firstMethod { name = "applyFlags" }.hook {
                    before {
                        val drawableCreationFlags = args(args.indexOfFirst { it is Int }).int()
                        val badgeInfo = firstField { name = "badgeInfo" }.of(instance).get()
                        val flag = firstField { name = "flags" }.of(instance).get<Int>()
                            ?: return@before
                        //flag & 2 != 0 -> ic_instant_app_badge 即时应用程序
                        //flag & 16 != 0 -> ic_archive_app_badge 存档应用程序
                        //flag & 4 != 0 -> ic_oplus_clone_app_badge 分身应用程序
                        //flag & 1 != 0 -> ic_work_app_badge 工作应用程序
                        if ((drawableCreationFlags and 2) == 0) {
                            if (badgeInfo != null) {
                                if (isShortcut) resultNull()
                            }
                            if ((flag and 2) != 0) {
                                //ic_instant_app_badge
                                //resultNull()
                            }
                            if ((flag and 16) != 0) {
                                //ic_archive_app_badge
                                //resultNull()
                            }
                            if ((flag and 4) == 0) {
                                if ((flag and 1) != 0) {
                                    //ic_work_app_badge
                                    if (isWork) resultNull()
                                } else if ((flag and 4) != 0) {
                                    //ic_clone_app_badge
                                    if (isClone) resultNull()
                                }
                            } else {
                                //ic_oplus_clone_app_badge_new
                                if (isClone) resultNull()
                            }
                        }
                    }
                }
            }

            //Source CacheUtils
            "com.android.common.util.CacheUtils".toClass().resolve().apply {
                firstMethod {
                    name = "getCloneAppDrawable"
                    returnType = Drawable::class
                }.hook {
                    after {
                        if (isClone) resultNull()
                    }
                }
            }
        }
    }

    @Obfuscate
    object AppBadgeC13 : Hooker {
        override fun onHook() {
            val isShortcut = prefs(ModulePrefs).getBoolean("remove_app_shortcut_badge", false)
            val isWork = prefs(ModulePrefs).getBoolean("remove_app_work_badge", false)
            val isClone = prefs(ModulePrefs).getBoolean("remove_app_clone_badge", false)

            //Source BitmapInfo
            "com.android.launcher3.icons.BitmapInfo".toClass().resolve().apply {
                firstMethod { name = "applyFlags"; parameterCount = 3 }.hook {
                    before {
                        val drawableCreationFlags = args().last().int()
                        val badgeInfo = firstField { name = "badgeInfo" }.of(instance).get()
                        val flag = firstField { name = "flags" }.of(instance).get<Int>()
                            ?: return@before
                        if ((drawableCreationFlags and 2) == 0) {
                            if (badgeInfo != null) {
                                if (isShortcut) resultNull()
                            } else if ((flag and 2) != 0) {
                                //ic_instant_app_badge
                                //resultNull()
                            } else if ((flag and 1) != 0) {
                                //ic_work_app_badge
                                if (isWork) resultNull()
                            } else if ((flag and 4) != 0) {
                                //ic_oplus_clone_app_badge
                                if (isClone) resultNull()
                            }
                        }
                    }
                }
            }
        }
    }
}