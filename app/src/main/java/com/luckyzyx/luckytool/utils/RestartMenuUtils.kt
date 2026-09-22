package com.luckyzyx.luckytool.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Process
import android.os.RemoteException
import android.widget.TextView
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import com.drake.net.utils.scope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.highcapable.betterandroid.ui.extension.view.layoutInflater
import com.luckyzyx.luckytool.IPackageServiceController
import com.luckyzyx.luckytool.R
import com.luckyzyx.luckytool.databinding.DialogReoptimizeDexLayoutBinding
import com.luckyzyx.luckytool.service.ActivityManagerService
import com.luckyzyx.luckytool.service.PackagesService
import com.luckyzyx.luckytool.service.PowerService
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RestartMenuUtils {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * 重启作用域对话框
     * @receiver Context
     */
    fun showMainRestartMenu(context: Context) {
        val list = arrayOf(
            context.getString(R.string.restart_scope),
            context.getString(R.string.re_optimize_dex),
            context.getString(R.string.reboot),
            context.getString(R.string.fast_reboot)
        )
        MaterialAlertDialogBuilder(context, dialogCentered).apply {
            setCancelable(true)
            setItems(list) { _: DialogInterface?, i: Int ->
                when (i) {
                    0 -> showRestartAllScopeDialog(context)
                    1 -> showOptimizeAllDexDialog(context)
                    2 -> {
                        PowerService.get(context) { controller ->
                            controller?.reboot(false, null, false)
                        }
                    }

                    3 -> ShellUtils.fastCmd(CommandUtils.killzygote)
                }
            }
            show()
        }
    }

    /**
     * 重启部分作用域对话框
     * @receiver Context
     * @param scopes Array<String>
     */
    fun showRestartScopeDialog(context: Context, scopes: Array<String>) {
        if (scopes.isEmpty()) return
        val list = arrayOf(
            context.getString(R.string.restart_scope),
            context.getString(R.string.re_optimize_dex),
            context.getString(R.string.restart_only_this_page_scope),
            context.getString(R.string.optimize_only_this_page_scope),
        )
        MaterialAlertDialogBuilder(context, dialogCentered).apply {
            setItems(list) { _, which ->
                when (which) {
                    0 -> showRestartAllScopeDialog(context)
                    1 -> showOptimizeAllDexDialog(context)
                    2 -> restartScope(context, scopes)
                    3 -> optimizeScope(context, scopes)
                }
            }
            show()
        }
    }

    /**
     * 重启全部作用域
     * @receiver Context
     */
    private fun showRestartAllScopeDialog(context: Context) {
        val xposedScope = context.resources.getStringArray(R.array.xposed_scope)
        MaterialAlertDialogBuilder(context).apply {
            setMessage(context.getString(R.string.restart_scope_message))
            setPositiveButton(context.getString(android.R.string.ok)) { _: DialogInterface?, _: Int ->
                scope(Dispatchers.Default) {
                    restartScope(context, xposedScope)
                }
            }
            setNeutralButton(context.getString(android.R.string.cancel), null)
            show()
        }
    }

    /**
     * 重启部分作用域
     * @receiver Context
     * @param scopes Array<String>
     */
    private fun restartScope(context: Context, scopes: Array<String>) {
        val killSystemUI = scopes.contains("com.android.systemui")
        AppUtils(context).getAllAppVerInfo(scopes)
        ActivityManagerService.get(context) { controller ->
            scopes.forEachIndexed { _, packName ->
                if (packName == "com.android.systemui") return@forEachIndexed
                val uid = Process.myUid() / 100000
                controller?.forceStopPackage(packName, uid)
            }
        }
        if (killSystemUI) ShellUtils.fastCmd(CommandUtils.killSysui)
    }


    /**
     * 重启选项
     * @param reason String
     */
    fun shellReboot(reason: String = "") {
        if (reason == "recovery") {
            // KEYCODE_POWER = 26, hide incorrect "Factory data reset" message
            Shell.getShell().newJob().add("/system/bin/input keyevent 26").exec()
        }
        Shell.getShell().newJob()
            .add("/system/bin/svc power reboot $reason || /system/bin/reboot $reason").exec()
    }

    /**
     * 重新优化全部作用域Dex
     * @receiver Context
     */
    fun showOptimizeAllDexDialog(context: Context, isForce: Boolean = false) {
        val scopeMaps = arrayMapOf<String, CharSequence>()
        context.resources.getStringArray(R.array.xposed_scope).toMutableList().apply {
            removeIf { it == "android" || it == "system" }
            removeIf { PackageUtils(context.packageManager).getPackageInfo(it, 0) == null }
            forEachIndexed { _, it ->
                val name = PackageUtils(context.packageManager).getApplicationInfo(it, 0)
                    ?.loadLabel(context.packageManager)
                scopeMaps[it] = name
            }
        }

        if (isForce) optimizeScopeDex(context, scopeMaps)
        else {
            MaterialAlertDialogBuilder(context).apply {
                setMessage(context.getString(R.string.re_optimize_dex_message))
                setPositiveButton(context.getString(android.R.string.ok)) { _: DialogInterface?, _: Int ->
                    optimizeScopeDex(context, scopeMaps)
                }
                setNeutralButton(context.getString(android.R.string.cancel), null)
                show()
            }
        }
    }

    /**
     * 优化部分作用域
     * @receiver Context
     * @param scopes Array<String>
     */
    fun optimizeScope(context: Context, scopes: Array<String>) {
        val scopeMaps = arrayMapOf<String, CharSequence>()
        scopes.toMutableList().apply {
            removeIf { it == "android" || it == "system" }
            removeIf { PackageUtils(context.packageManager).getPackageInfo(it, 0) == null }
            forEachIndexed { _, it ->
                val name = PackageUtils(context.packageManager).getApplicationInfo(it, 0)
                    ?.loadLabel(context.packageManager)
                scopeMaps[it] = name
            }
        }

        optimizeScopeDex(context, scopeMaps)
    }

    private fun optimizeScopeDex(
        context: Context,
        scopes: ArrayMap<String, CharSequence>
    ) {
        val binding = DialogReoptimizeDexLayoutBinding.inflate(context.layoutInflater)
        val progressDialog = MaterialAlertDialogBuilder(context, dialogCentered).apply {
            setTitle(context.getString(R.string.re_optimize_dex_optimizing))
            setView(binding.root)
            setCancelable(false)
        }.create()
        val textView = binding.tv

        PackagesService.get(context) { controller ->
            coroutineScope.launch {
                progressDialog.show()
                val failedApps = try {
                    AppUtils(context).getAllAppVerInfo(scopes.keys.toTypedArray(), true)
                    optimizeApps(controller, scopes, textView)
                } finally {
                    progressDialog.dismiss()
                }
                if (failedApps.isNotEmpty()) {
                    showDexRetryDialog(context, failedApps)
                } else {
                    context.showToast(context.getString(R.string.re_optimize_dex_completed))
                }
            }
        }
    }

    /**
     * 优化多个应用，动态更新进度信息
     */
    @SuppressLint("SetTextI18n")
    private suspend fun optimizeApps(
        controller: IPackageServiceController?,
        scopes: ArrayMap<String, CharSequence>, textView: TextView
    ): ArrayMap<String, CharSequence> {
        val failedApps = arrayMapOf<String, CharSequence>()
        withContext(Dispatchers.IO) {
            scopes.keys.forEachIndexed { index, pack ->
                val name = scopes[pack] ?: pack
                withContext(Dispatchers.Main) {
                    textView.text = "$name (${index + 1}/${scopes.size})"
                }
                val success = try {
                    controller?.clearApplicationProfileData(pack)
                    controller?.performDexOptMode(pack) == true
                } catch (e: RemoteException) {
                    LogUtils.e("performAllScopeDex", pack, e.toString(), true)
                    false
                }
                if (success) {
                    LogUtils.d("performAllScopeDex", pack, "success", true)
                } else {
                    LogUtils.e("performAllScopeDex", pack, "fail", true)
                    failedApps[pack] = scopes[pack]
                }
            }
        }
        return failedApps
    }

    /**
     * 显示重新优化对话框
     */
    private fun showDexRetryDialog(
        context: Context,
        failedApps: ArrayMap<String, CharSequence>
    ) {
        MaterialAlertDialogBuilder(context, dialogCentered).apply {
            setTitle(context.getString(R.string.re_optimize_dex_failed))
            setMessage(
                context.getString(
                    R.string.re_optimize_dex_faile_message,
                    failedApps.values.joinToString("\n")
                )
            )
            setPositiveButton(context.getString(android.R.string.ok)) { _, _ ->
                optimizeScopeDex(context, failedApps)
            }
            setNeutralButton(context.getString(android.R.string.cancel), null)
            show()
        }
    }

}
