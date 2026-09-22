package com.luckyzyx.luckytool.hook.hookers

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Env
import com.luckyzyx.luckytool.hook.core.Hooker
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
            if (allowDowngrade || skipScan) {
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
                api.deoptimize(check)
            }
            feature("skip_apk_scan") {
                val repositoryClass = "com.oplus.appdetail.model.guide.repository.riskScan.RiskRepository".toClass(appClassLoader)
                val parseResult = repositoryClass.declaredMethods.single {
                    !Modifier.isStatic(it.modifiers) && it.parameterTypes.contentEquals(arrayOf(Bundle::class.java)) &&
                        it.returnType != Void.TYPE
                }.apply { isAccessible = true }
                // 替换一次扫描任务的结果，保留外层 Flow 的结果派发和界面生命周期。
                val task = "com.oplus.appdetail.model.guide.repository.riskScan.RiskRepository\$scanRiskFlow\$2\$result\$1".toClass(appClassLoader)
                val repository = task.declaredFields.single { it.type == repositoryClass }.apply { isAccessible = true }
                task.getDeclaredMethod("invokeSuspend", Any::class.java).hookMethod {
                    before { result = parseResult.invoke(repository.get(instance), Bundle()) }
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
                val clicked = java.util.Collections.newSetFromMap(java.util.WeakHashMap<View, Boolean>())
                bottom.declaredMethods.single {
                    !Modifier.isStatic(it.modifiers) && it.parameterTypes.size == 3 &&
                        it.parameterTypes[0] == List::class.java
                }.hookMethod {
                    after {
                        val view = instance as View
                        view.post {
                            if (!view.isAttachedToWindow || clicked.contains(view)) return@post
                            val id = view.resources.getIdentifier("ad_continue_install", "string", packageName)
                            val label = view.resources.getString(id)
                            descendants(view).filterIsInstance<Button>().firstOrNull {
                                it.isShown && it.isEnabled && it.text.toString() == label
                            }?.let {
                                clicked.add(view)
                                it.performClick()
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
                "com.oplus.appdetail.model.uninstall.UninstallPackageActivity".toClass(appClassLoader)
                    .getDeclaredMethod("onCreate", Bundle::class.java).hookMethod {
                        after {
                            val activity = instance as Activity
                            val root = activity.window.decorView
                            var clicked = false
                            val listener = ViewTreeObserver.OnGlobalLayoutListener {
                                if (!activity.isFinishing) {
                                    val done = root.resources.getIdentifier("uninstall_success_hint", "string", packageName)
                                    if (descendants(root).filterIsInstance<TextView>().any {
                                            it.isShown && it.text.toString() == root.resources.getString(done)
                                        }) activity.finish()
                                    else if (!clicked) {
                                        val id = root.resources.getIdentifier("btn_uninstall", "id", packageName)
                                        root.findViewById<View>(id)?.takeIf { it.isShown && it.isEnabled }?.let {
                                            clicked = true
                                            it.post { if (!activity.isFinishing) it.performClick() }
                                        }
                                    }
                                }
                            }
                            root.viewTreeObserver.addOnGlobalLayoutListener(listener)
                            root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                                override fun onViewAttachedToWindow(v: View) = Unit
                                override fun onViewDetachedFromWindow(v: View) {
                                    v.viewTreeObserver.removeOnGlobalLayoutListener(listener)
                                    v.removeOnAttachStateChangeListener(this)
                                }
                            })
                        }
                    }
            }
        }
    }

    private inline fun feature(key: String, install: () -> Unit) {
        if (!prefs(ModulePrefs).getBoolean(key, false)) return
        runCatching(install).onFailure { Env.log(android.util.Log.ERROR, "AppDetail", "Cannot hook $key", it) }
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
