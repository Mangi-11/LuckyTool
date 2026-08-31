# 模块资源注入

宿主进程内使用模块资源（`R.string` / `R.dimen` / `R.drawable`，包 id 0x64 段）时，需要把模块 APK 挂进宿主的 AssetManager——对齐 legacy YukiHookAPI `AppParasitics.injectModuleAppResources` 的实现（反射 `AssetManager.addAssetPath(模块 APK 路径)`）。

## 不能用的方式

```kotlin
// ❌ NameNotFoundException：LSPosed 隐藏模块包名，宿主的 PackageManager 查不到
packageManager.getResourcesForApplication(BuildConfig.APPLICATION_ID)
```

## 正确方式

模块 APK 路径来自 `XposedModule.getModuleApplicationInfo().sourceDir`（`onModuleLoaded` 时存入 `Env.moduleAppInfo`）：

```kotlin
fun Context.injectModuleAppResources() {
    val modulePath = Env.moduleAppInfo?.sourceDir ?: return  // 打 warning
    runCatching {
        AssetManager::class.java
            .getDeclaredMethod("addAssetPath", String::class.java)
            .apply { isAccessible = true }
            .invoke(resources.assets, modulePath)
    }.onFailure { XLog.w("injectModuleAppResources failed", t = it) }
}
```

挂载后宿主 Resources **原生**可解析模块资源 id（含 `getDimensionPixelSize`、`getDrawable`、`getString` 全系列），无需 wrapper。

## 使用场景

- 功能页把模块字样/尺寸注入宿主 UI（如通知对齐宽度、状态栏尺寸）
- hook 回调里读 `R.dimen/R.string` 实现宿主 UI 适配

::: warning
`addAssetPath` 是幂等操作（重复挂载无副作用）；失败时只打 warn 不影响其余 Hook。
:::