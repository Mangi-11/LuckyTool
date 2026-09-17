package com.luckyzyx.luckytool.ui.fragment.scopes.related

import android.content.Context
import androidx.navigation.fragment.findNavController
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.luckyzyx.luckytool.R
import com.luckyzyx.luckytool.ui.activity.MainActivity
import com.luckyzyx.luckytool.ui.fragment.base.BaseScopePreferenceFeagment
import com.luckyzyx.luckytool.utils.A13
import com.luckyzyx.luckytool.utils.ModulePrefs
import com.luckyzyx.luckytool.utils.SDK
import com.luckyzyx.luckytool.utils.arraySummaryDot
import com.luckyzyx.luckytool.utils.arraySummaryLine
import com.luckyzyx.luckytool.utils.checkPackName
import com.luckyzyx.luckytool.utils.getBoolean
import com.luckyzyx.luckytool.utils.getString
import com.luckyzyx.luckytool.utils.navigatePage
import com.luckyzyx.luckytool.utils.sendPrefsValue
import com.luckyzyx.luckytool.utils.setPrefsIconRes
import com.luckyzyx.luckytool.utils.setSummaryProvider
import com.topjohnwu.superuser.ShellUtils
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class LauncherRelated : BaseScopePreferenceFeagment() {
    override val scopes = arrayOf("com.android.launcher", "com.oppo.launcher")

    override val isEnableRestartMenu: Boolean = true

    override val currentPrefsName: String = ModulePrefs

    override val navigateFragmentId: Int = R.id.launcher

    override fun Context.loadRootPreference(): Preference {
        return Preference(this).apply {
            key = "com.android.launcher"
            setPrefsIconRes(key) { resource, show ->
                icon = resource
                isIconSpaceReserved = show
            }
            title = getString(R.string.Desktop)
            summary = arraySummaryDot(
                getString(R.string.AppBadgeRelated),
                getString(R.string.FolderLayoutRelated),
                getString(R.string.launcher_layout_related)
            )
            isVisible = checkPackName(key)
        }
    }

    override fun Context.loadPreferences(): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            //小组件
            add(PreferenceCategory(this@loadPreferences).apply {
                title = getString(R.string.WidgetRelated)
                key = "WidgetRelated"
                isVisible = osCode >= 26
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.remove_launcher_card_name)
                key = "remove_launcher_card_name"
                setDefaultValue(false)
                isVisible = osCode >= 26
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.remove_widgets_add_request_whitelist)
                key = "remove_widgets_add_request_whitelist"
                setDefaultValue(false)
                isVisible = osCode >= 30
                isIconSpaceReserved = false
            })
            //应用图标
            add(PreferenceCategory(this@loadPreferences).apply {
                title = getString(R.string.AppIcon)
                key = "AppIcon"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.allow_app_names_display_multiple_lines)
                key = "allow_app_names_display_multiple_lines"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (getBoolean(ModulePrefs, "allow_app_names_display_multiple_lines", false)) {
                add(SeekBarPreference(this@loadPreferences).apply {
                    title = getString(R.string.custom_app_icon_name_line_height)
                    key = "custom_app_icon_name_line_height"
                    setDefaultValue(-1)
                    max = 15
                    min = -1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
            }
            add(SeekBarPreference(this@loadPreferences).apply {
                title = getString(R.string.custom_launcher_app_icon_size)
                summary = getString(R.string.statusbar_clock_if_zero_summary)
                key = "custom_launcher_app_icon_size"
                setDefaultValue(0)
                max = 100
                min = 0
                showSeekBarValue = true
                updatesContinuously = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.disable_long_press_app_icon_secondary_menu)
                key = "disable_long_press_app_icon_secondary_menu"
                setDefaultValue(false)
                isVisible = osCode >= 37
                isIconSpaceReserved = false
            })
            //应用徽标
            add(PreferenceCategory(this@loadPreferences).apply {
                title = getString(R.string.AppBadgeRelated)
                key = "AppBadgeRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_display_app_update_dot)
                summary = getString(R.string.enable_display_app_update_dot_summary)
                key = "enable_display_app_update_dot"
                setDefaultValue(false)
                isVisible = osCode < 33
                isIconSpaceReserved = false
            })
            add(DropDownPreference(this@loadPreferences).apply {
                title = getString(R.string.set_app_update_dot_display_mode)
                key = "set_app_update_dot_display_mode"
                val summaryLines = arrayListOf(
                    getString(R.string.current_mode) + ": %s"
                )
                val value = getString(ModulePrefs, key, "0")
                when (value) {
                    "1" -> summaryLines.add(getString(R.string.need_restart_system))
                    "2" -> summaryLines.add(getString(R.string.need_restart_scope))
                }
                summary = arraySummaryLine(*summaryLines.toTypedArray())

                setEntries(R.array.set_app_update_dot_display_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                isVisible = osCode >= 33
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (SDK >= A13) {
                add(SwitchPreference(this@loadPreferences).apply {
                    title = getString(R.string.remove_app_shortcut_badge)
                    key = "remove_app_shortcut_badge"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(this@loadPreferences).apply {
                    title = getString(R.string.remove_app_work_badge)
                    key = "remove_app_work_badge"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(this@loadPreferences).apply {
                    title = getString(R.string.remove_app_clone_badge)
                    key = "remove_app_clone_badge"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
            }
            //文件夹布局
            add(PreferenceCategory(this@loadPreferences).apply {
                title = getString(R.string.FolderLayoutRelated)
                key = "FolderLayoutRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.remove_folder_name_input_limit)
                key = "remove_folder_name_input_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_auto_close_folder)
                key = "enable_auto_close_folder"
                setDefaultValue(false)
                isVisible = osCode >= 34
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.remove_folder_preview_background)
                key = "remove_folder_preview_background"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_folder_layout_adjustment)
                key = "enable_folder_layout_adjustment"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (getBoolean(ModulePrefs, "enable_folder_layout_adjustment", false)) {
                add(SeekBarPreference(this@loadPreferences).apply {
                    title = getString(R.string.set_icon_rows_in_folder)
                    key = "set_icon_rows_in_folder"
                    setDefaultValue(4)
                    max = 10
                    min = 4
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(this@loadPreferences).apply {
                    title = getString(R.string.set_icon_columns_in_folder)
                    key = "set_icon_columns_in_folder"
                    setDefaultValue(3)
                    max = 10
                    min = 3
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(this@loadPreferences).apply {
                    title = getString(R.string.sync_folder_icon_column_number_preview)
                    key = "sync_folder_icon_column_number_preview"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
            }
            //分页组件
            add(PreferenceCategory(this@loadPreferences).apply {
                title = getString(R.string.PaginationComponentRelated)
                key = "PaginationComponentRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.remove_pagination_component)
                key = "remove_pagination_component"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.remove_folder_pagination_component)
                key = "remove_folder_pagination_component"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.disable_pagination_component_sliding)
                key = "disable_pagination_component_sliding"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            //最近任务列表
            add(PreferenceCategory(this@loadPreferences).apply {
                title = getString(R.string.RecentTaskListRelated)
                key = "RecentTaskListRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_recent_task_pin_capsule)
                key = "enable_recent_task_pin_capsule"
                setDefaultValue(false)
                isVisible = osCode >= 37
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.force_enable_recent_task_memory_display)
                key = "force_enable_recent_task_memory_display"
                setDefaultValue(false)
                isVisible = osCode >= 30
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.disable_auto_switch_last_task)
                key = "disable_auto_switch_last_task"
                setDefaultValue(false)
                isVisible = osCode >= 34
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_stacked_task_layout)
                key = "enable_stacked_task_layout"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (getBoolean(ModulePrefs, "enable_stacked_task_layout", false)) {
                add(SeekBarPreference(this@loadPreferences).apply {
                    title = getString(R.string.set_task_stacking_level)
                    key = "set_task_stacking_level"
                    setDefaultValue(7)
                    max = 10
                    min = 5
                    showSeekBarValue = true
                    updatesContinuously = false
                    isVisible = false
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(this@loadPreferences).apply {
                    title = getString(R.string.fix_current_task_to_the_top)
                    key = "fix_current_task_to_the_top"
                    setDefaultValue(false)
                    isVisible = false
                    isIconSpaceReserved = false
                })
            }
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.long_press_app_icon_open_app_details)
                key = "long_press_app_icon_open_app_details"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.remove_bottom_app_icon_of_recent_task_list)
                key = "remove_bottom_app_icon_of_recent_task_list"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.remove_recent_task_list_clear_button)
                key = "remove_recent_task_list_clear_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.unlock_task_locks)
                key = "unlock_task_locks"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.allow_locking_unlocking_of_excluded_activity)
                key = "allow_locking_unlocking_of_excluded_activity"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(DropDownPreference(this@loadPreferences).apply {
                title = getString(R.string.custom_app_floating_window_display_mode)
                summary = getString(R.string.current_mode) + ": %s"
                key = "custom_app_floating_window_display_mode"
                setEntries(R.array.custom_app_floating_window_display_mode_entries)
                entryValues = arrayOf("0", "1", "2", "3")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    sendPrefsValue("android", key, newValue)
                    if ((newValue.toString().toIntOrNull() ?: 0) >= 2) {
                        ShellUtils.fastCmd("settings put global enable_non_resizable_multi_window 1")
                    }
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (getString(
                    ModulePrefs, "custom_app_floating_window_display_mode", "0"
                ) == "3"
            ) {
                add(Preference(this@loadPreferences).apply {
                    title = getString(R.string.zoom_window_support_list)
                    summary = getString(R.string.zoom_window_support_list_summary)
                    key = "zoom_window_support_list"
                    isIconSpaceReserved = false
                    setOnPreferenceClickListener {
                        findNavController().navigatePage(R.id.zoomWindowFragment, title)
                        true
                    }
                })
            }
            if (osCode >= 33) {
                add(SwitchPreference(this@loadPreferences).apply {
                    title = getString(R.string.force_enable_multi_window_mode)
                    summary = getString(R.string.need_restart_system)
                    key = "force_enable_multi_window_mode"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        sendPrefsValue("android", key, newValue)
                        true
                    }
                })
                add(SeekBarPreference(this@loadPreferences).apply {
                    title = getString(R.string.custom_multi_window_display_upper_limit)
                    key = "custom_multi_window_display_upper_limit"
                    setDefaultValue(2)
                    max = 20
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        sendPrefsValue("android", key, newValue)
                        true
                    }
                })
            }
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.force_all_apps_support_split_screen)
                key = "force_all_apps_support_split_screen"
                setDefaultValue(false)
                isVisible = osCode in 26..33
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    sendPrefsValue("android", key, newValue)
                    true
                }
            })
            //抽屉布局
            add(PreferenceCategory(this@loadPreferences).apply {
                title = getString(R.string.launcher_drawer_layout_related)
                key = "DrawerLayoutRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_drawer_layout_adjustment)
                key = "enable_drawer_layout_adjustment"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (getBoolean(ModulePrefs, "enable_drawer_layout_adjustment", false)) {
                add(SeekBarPreference(this@loadPreferences).apply {
                    title = getString(R.string.set_icon_columns_in_drawer)
                    key = "set_icon_columns_in_drawer"
                    setDefaultValue(4)
                    max = 10
                    min = 4
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
            }
            //桌面布局
            add(PreferenceCategory(this@loadPreferences).apply {
                title = getString(R.string.launcher_layout_related)
                key = "DesktopLayoutRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_launcher_indicator_entry)
                key = "enable_launcher_indicator_entry"
                setDefaultValue(false)
                isVisible = osCode >= 37
                isIconSpaceReserved = false
            })
            add(EditTextPreference(this@loadPreferences).apply {
                title = getString(R.string.custom_desktop_default_home_page)
                dialogTitle = title
                key = "custom_desktop_default_home_page"
                setDefaultValue("0")
                setSummaryProvider(this)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.enable_docker_background)
                key = "enable_docker_background"
                setDefaultValue(false)
                isVisible = osCode >= 26
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.force_enable_docker_background_blur)
                key = "force_enable_docker_background_blur"
                setDefaultValue(false)
                isVisible = osCode >= 37
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.remove_docker_max_number_limit)
                summary = getString(R.string.remove_docker_max_number_limit_summary)
                key = "remove_docker_max_number_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(this@loadPreferences).apply {
                title = getString(R.string.launcher_layout_enable)
                summary = getString(R.string.launcher_layout_row_colume)
                key = "launcher_layout_enable"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (getBoolean(ModulePrefs, "launcher_layout_enable", false)) {
                add(SeekBarPreference(this@loadPreferences).apply {
                    title = getString(R.string.launcher_layout_max_rows)
                    key = "launcher_layout_max_rows"
                    setDefaultValue(6)
                    max = 11
                    min = 6
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(this@loadPreferences).apply {
                    title = getString(R.string.launcher_layout_max_columns)
                    if (osCode >= 30) summary =
                        getString(R.string.launcher_layout_max_columns_summary)
                    key = "launcher_layout_max_columns"
                    setDefaultValue(4)
                    max = 9
                    min = 4
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
            }
            //桌面事件
            add(PreferenceCategory(this@loadPreferences).apply {
                title = getString(R.string.launcher_events)
                key = "LauncherEvents"
                isVisible = false
                isIconSpaceReserved = false
            })
        }
    }
}