package com.luckyzyx.luckytool.hook.hookers

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Env
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.scopes.appdetail.PendingInstallerAction
import com.luckyzyx.luckytool.hook.scopes.appdetail.InstallFinishPage
import com.luckyzyx.luckytool.hook.scopes.appdetail.CancelInstallPage
import com.luckyzyx.luckytool.hook.scopes.appdetail.ApkDetailsView
import com.luckyzyx.luckytool.hook.core.hookMethod
import com.luckyzyx.luckytool.utils.DexkitUtils
import com.luckyzyx.luckytool.utils.ModulePrefs
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Modifier

/** ColorOS 的新版安装界面由 AppDetail 独立进程承载。 */
object HookAppDetail : Hooker {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { bridge ->
            val allowDowngrade = prefs(ModulePrefs).getBoolean("allow_downgrade_install", false)
            val skipScan = prefs(ModulePrefs).getBoolean("skip_apk_scan", false)
            register("install_preflight", allowDowngrade || skipScan) {
                val check = bridge.findMethod {
                    matcher { usingStrings("installedVerName", "installedUnionGameVerName") }
                }.single().getMethodInstance(appClassLoader)
                // 仅在安装前版本提示检查内，忽略当前 APK 的已安装版本；不修改 APK 元数据。
                // 已核对该方法第一次查询为当前包，后续查询为应用市场推荐的联运包。
                val firstVersionQuery = ThreadLocal<Boolean>()
                val api = Env.requireBase()
                api.hook(check).setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept { chain ->
                        val previous = firstVersionQuery.get()
                        firstVersionQuery.set(allowDowngrade)
                        try {
                            chain.proceed()
                        } finally {
                            if (previous == null) firstVersionQuery.remove()
                            else firstVersionQuery.set(previous)
                        }
                    }
                "com.nearme.common.util.AppUtil".toClass(appClassLoader)
                    .getDeclaredMethod("getAppVersionCode", Context::class.java, String::class.java)
                    .hookMethod {
                        before {
                            if (firstVersionQuery.get() == true) {
                                firstVersionQuery.set(false)
                                result = -1
                            }
                        }
                    }
                if (skipScan) {
                    // 只跳过本次安装的信息收集提醒，不向系统写入“始终允许”记录。
                    "com.oplus.appdetail.model.entrance.ChannelConfigSp".toClass(appClassLoader)
                        .declaredMethods.single {
                            it.returnType == Boolean::class.javaPrimitiveType &&
                                it.parameterTypes.contentEquals(arrayOf(String::class.java))
                        }.hookMethod {
                            before { if (firstVersionQuery.get() != null) resultTrue() }
                        }
                }
                if (allowDowngrade) {
                    // The market-version promotion is independent of the installed-version check.
                    "com.heytap.cdo.security.domain.safeguide.GuideContent".toClass(appClassLoader)
                        .getDeclaredMethod("getVersionGuidePopupInfo").hookMethod {
                            before { if (firstVersionQuery.get() != null) resultNull() }
                        }
                }
                api.deoptimize(check)
            }
            feature("skip_apk_scan") {
                val repositoryClass = "com.oplus.appdetail.model.guide.repository.riskScan.RiskRepository".toClass(appClassLoader)
                val modelClass = "com.oplus.appdetail.modelv2.guide.viewmodel.RiskScanViewModel".toClass(appClassLoader)
                val paramClass = "com.oplus.appdetail.model.guide.repository.ExtJumpParam".toClass(appClassLoader)
                val guideClass = "com.heytap.cdo.security.domain.safeguide.GuideResult".toClass(appClassLoader)
                val parse = bridge.findMethod {
                    matcher { declaredClass(repositoryClass); paramTypes(Bundle::class.java) }
                }.single()
                val stateType = parse.getMethodInstance(appClassLoader).returnType
                val doneField = parse.usingFields.map { it.field.getFieldInstance(appClassLoader) }.single {
                    Modifier.isStatic(it.modifiers) && stateType.isAssignableFrom(it.type)
                }.apply { isAccessible = true }
                val stateField = bridge.findMethod {
                    matcher { declaredClass(modelClass); paramCount(0); returnType(List::class.java) }
                }.single().usingFields.map { it.field.getFieldInstance(appClassLoader) }.single {
                    it.type.name == "androidx.lifecycle.MutableLiveData"
                }.apply { isAccessible = true }
                val paramField = modelClass.declaredFields.single { it.type == paramClass }.apply { isAccessible = true }
                val postValue = stateField.type.getMethod("postValue", Any::class.java)
                // Complete the ViewModel state itself, not just the remote worker: the original
                // coroutine also runs scan animations, minimum-duration timers and security checks.
                modelClass.declaredMethods.single {
                    it.parameterTypes.contentEquals(arrayOf(paramClass, guideClass, Long::class.javaPrimitiveType))
                }.hookMethod {
                    before {
                        paramField.set(instance, args[0])
                        postValue.invoke(stateField.get(instance), doneField.get(null))
                        resultNull()
                    }
                }
                val scanContainer = "com.oplus.appdetail.modelv2.guide.view.ScanContentFrameLayout".toClass(appClassLoader)
                scanContainer.declaredConstructors.forEach { constructor ->
                    constructor.hookMethod { after { (instance as View).visibility = View.GONE } }
                }
                // The host may show the container again after an asynchronous scan-state update.
                View::class.java.getDeclaredMethod("setVisibility", Int::class.javaPrimitiveType).hookMethod {
                    before { if (scanContainer.isInstance(instanceOrNull)) args(0).set(View.GONE) }
                }
            }
            feature("remove_install_ads") {
                val manager = "com.oplus.appdetail.model.finish.manager.RecommendManager".toClass(appClassLoader)
                manager.declaredMethods.filter {
                    !Modifier.isStatic(it.modifiers) && it.returnType == Any::class.java &&
                        it.parameterTypes.size == 2 && it.parameterTypes.last().methods.any { method -> method.name == "resumeWith" }
                }.single().hookMethod {
                    // 宿主本身将 null 作为无推荐内容处理。
                    before { resultNull() }
                }
            }
            feature("remove_install_ads") {
                "com.oplus.appdetail.model.cancel.CancelInstallActivity".toClass(appClassLoader)
                    .getDeclaredMethod("onCreate", Bundle::class.java).hookMethod {
                        after { CancelInstallPage.simplify(instance as Activity) }
                    }
            }
            feature("remove_install_ads") {
                "com.oplus.appdetail.model.finish.InstallFinishActivity".toClass(appClassLoader)
                    .getDeclaredMethod("onCreate", Bundle::class.java).hookMethod {
                        after { InstallFinishPage.hideStorePromotion(instance as Activity) }
                    }
            }
            feature("show_more_apk_package_information") {
                val paramClass = "com.oplus.appdetail.model.guide.repository.ExtJumpParam".toClass(appClassLoader)
                val headerClass = "com.oplus.appdetail.modelv2.guide.view.HeaderAppInfoView".toClass(appClassLoader)
                headerClass.declaredMethods.single {
                    !Modifier.isStatic(it.modifiers) && it.parameterTypes.contentEquals(arrayOf(paramClass))
                }.hookMethod {
                    after {
                        val view = instance as View
                        val param = args[0] ?: return@after
                        fun value(name: String) = paramClass.getDeclaredMethod(name).invoke(param)?.toString().orEmpty()
                        val pkg = value("getPkg")
                        val version = value("getAppVerName")
                        val code = value("getAppVerCode")
                        val path = value("getApkFilePath")
                        ApkDetailsView.show(view as ViewGroup, pkg, version, code, path)
                    }
                }
            }
            feature("auto_click_install_button") {
                val bottom = "com.oplus.appdetail.modelv2.guide.view.GuideBottomView".toClass(appClassLoader)
                val watched = java.util.Collections.newSetFromMap(java.util.WeakHashMap<View, Boolean>())
                bottom.declaredMethods.single {
                    !Modifier.isStatic(it.modifiers) && it.parameterTypes.size == 3 &&
                        it.parameterTypes[0] == List::class.java
                }.hookMethod {
                    after {
                        val view = instance as View
                        if (!watched.add(view)) return@after
                        PendingInstallerAction.watch(view) {
                            if (view.context.activity()?.isFinishing == true) true
                            else {
                                val id = view.resources.getIdentifier("ad_continue_install", "string", packageName)
                                val label = view.resources.getString(id)
                                descendants(view).filterIsInstance<Button>().firstOrNull {
                                    PendingInstallerAction.ready(it) && it.text.toString() == label
                                }?.performClick() == true
                            }
                        }
                    }
                }
                val success = "com.oplus.appdetail.model.install.view.ProcessSuccessLayout".toClass(appClassLoader)
                success.declaredMethods.single {
                    it.parameterTypes.contentEquals(arrayOf(Int::class.javaPrimitiveType, String::class.java))
                }.hookMethod {
                    after {
                        val view = instance as View
                        if (args[0] == view.resources.getIdentifier("install_done", "string", packageName)) {
                            view.post { view.context.activity()?.finish() }
                        }
                    }
                }
            }
            feature("auto_click_uninstall_button") {
                val activityClass = "com.oplus.appdetail.model.uninstall.UninstallPackageActivity".toClass(appClassLoader)
                val modelClass = "com.oplus.appdetail.model.uninstall.viewmodel.UninstallViewModel".toClass(appClassLoader)
                val presenterClass = bridge.findClass {
                    matcher { usingStrings("activityBinding.btnUninstall") }
                }.single().name.toClass(appClassLoader)
                val activityField = presenterClass.declaredFields.single { it.type == activityClass }.apply { isAccessible = true }
                val modelField = presenterClass.declaredFields.single { it.type == modelClass }.apply { isAccessible = true }
                val uninstall = modelClass.declaredMethods.single {
                    it.returnType == Void.TYPE && it.parameterTypes.contentEquals(arrayOf(Activity::class.java))
                }.apply { isAccessible = true }
                // Only the selected app's retain/uninstall confirmation. Keep the host's own
                // uninstall operation and options instead of starting a separate package operation.
                presenterClass.declaredMethods.single {
                    !Modifier.isStatic(it.modifiers) && it.returnType == Void.TYPE &&
                        it.parameterTypes.contentEquals(arrayOf(String::class.java))
                }.hookMethod {
                    before {
                        uninstall.invoke(modelField.get(instance), activityField.get(instance))
                        resultNull()
                    }
                }
                "com.oplus.appdetail.model.uninstall.UninstallPackageActivity".toClass(appClassLoader)
                    .getDeclaredMethod("onCreate", Bundle::class.java).hookMethod {
                        after {
                            val activity = instance as Activity
                            val root = activity.window.decorView
                            var clicked = false
                            PendingInstallerAction.watch(root) {
                                if (activity.isFinishing) true
                                else {
                                    val done = root.resources.getIdentifier("uninstall_success_hint", "string", packageName)
                                    if (descendants(root).filterIsInstance<TextView>().any {
                                            it.isShown && it.text.toString() == root.resources.getString(done)
                                        }) {
                                        activity.finish()
                                        true
                                    } else {
                                        if (!clicked) {
                                            val id = root.resources.getIdentifier("btn_uninstall", "id", packageName)
                                            val button = root.findViewById<View>(id)
                                            val promptId = root.resources.getIdentifier("uninstall_consult_hint", "string", packageName)
                                            val ready = descendants(root).filterIsInstance<TextView>().any {
                                                it.isShown && it.text.toString() == root.resources.getString(promptId)
                                            }
                                            if (ready && button != null && PendingInstallerAction.ready(button)) {
                                                clicked = button.performClick()
                                            }
                                        }
                                        false
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    private inline fun feature(key: String, install: () -> Unit) =
        register(key, prefs(ModulePrefs).getBoolean(key, false), install)

    private inline fun register(name: String, enabled: Boolean, install: () -> Unit) {
        if (!enabled) return
        runCatching(install).onFailure {
            Env.log(android.util.Log.ERROR, "AppDetail", "Cannot hook $name", it)
        }
    }

    private fun descendants(view: View): Sequence<View> = sequence {
        yield(view)
        if (view is ViewGroup) for (i in 0 until view.childCount) yieldAll(descendants(view.getChildAt(i)))
    }

    private fun Context.activity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            val base = context.baseContext
            if (base === context) break
            context = base
        }
        return null
    }
}
