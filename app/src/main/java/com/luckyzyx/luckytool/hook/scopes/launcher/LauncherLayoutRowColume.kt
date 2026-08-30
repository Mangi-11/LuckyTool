package com.luckyzyx.luckytool.hook.scopes.launcher

import android.util.Pair
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.ArrayClass
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.get
import com.luckyzyx.luckytool.hook.core.hook
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.getOSVersionCode
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object LauncherLayoutRowColume : Hooker {
    override fun onHook() {
        val osCode = getOSVersionCode
        if (osCode >= 37) loadHooker(LayoutRowColume)
        else loadHooker(LayoutRowColumeV15)
    }

    @Obfuscate
    object LayoutRowColume : Hooker {
        override fun onHook() {
            val maxRows = prefs(ModulePrefs).getInt("launcher_layout_max_rows", 6)
            val maxColumns = prefs(ModulePrefs).getInt("launcher_layout_max_columns", 4)

            //Source UiConfig
            "com.android.launcher.UiConfig".toClass().resolve().apply {
                firstMethod { name = "isSupportLayout" }.hook {
                    replaceToTrue()
                }
                firstMethod { name = "getSupportLayout" }.hook {
                    before {
                        result = ArrayList<Pair<Int, Pair<Int, Int>>>().apply {
                            for (col in 4..maxColumns) {
                                for (row in 6..maxRows) {
                                    add(Pair(col, Pair(row, row + 1)))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Obfuscate
    object LayoutRowColumeV15 : Hooker {
        override fun onHook() {
            //Source UiConfig
            "com.android.launcher.UiConfig".toClass().resolve().apply {
                firstMethod { name = "isSupportLayout" }.hook {
                    replaceToTrue()
                }
            }
            val maxRows = prefs(ModulePrefs).getInt("launcher_layout_max_rows", 6)
            val maxColumns = prefs(ModulePrefs).getInt("launcher_layout_max_columns", 4)
            //Source ToggleBarLayoutAdapter
            "com.android.launcher.togglebar.adapter.ToggleBarLayoutAdapter".toClass().resolve()
                .apply {
                    firstMethod { name = "initToggleBarLayoutConfigs" }.hook {
                        before {
                            firstField {
                                name = "MIN_MAX_COLUMN"
                                type = ArrayClass(Int::class)
                            }.get<IntArray>()?.set(1, maxColumns)
                            firstField {
                                name = "MIN_MAX_ROW"
                                type = ArrayClass(Int::class)
                            }.get<IntArray>()?.set(1, maxRows)
                        }
                    }
                }
        }
    }
}