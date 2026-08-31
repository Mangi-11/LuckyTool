# 编写 Hook

所有 Hook 文件实现 `Hooker` 接口，并用 `hook/core` 提供的同形 DSL（形态对齐历史 YukiHookAPI 用法，迁移与新增都是「import + 基类」级别的工作）。

## 最小示例

```kotlin
package com.luckyzyx.luckytool.hook.scopes.systemui

import com.highcapable.kavaref.extension.toClass
import com.luckyzyx.luckytool.hook.core.Hooker
import com.luckyzyx.luckytool.hook.core.hook

object RemoveSomething : Hooker {
    override fun onHook() {
        "com.android.systemui.statusbar.XxxController".toClass().resolve().apply {
            firstMethod { name = "isEnabled" }.hook {
                replaceToTrue()
            }
        }
    }
}
```

## 常用 DSL 速查

| 写法 | 说明 |
|---|---|
| `firstMethod { name = "..."; paramCount = N }` | 方法匹配（`firstMethodOrNull` 可空变体） |
| `.hook { before { } after { } }` | Hook 链（`HookAction` 接收者） |
| `replaceTo(v) / replaceToTrue() / replaceToFalse() / intercept()` | 替换/拦截 |
| `args()` / `args(0)` / `args().last().cast<T>()` / `args(1).set(x)` | 参数访问（`Args`） |
| `instance` / `instance<T>()` / `instanceOrNull` / `method` | 回调上下文（`HookCall`） |
| `result = x` / `resultTrue()` / `resultNull()` | 提前返回 |
| `hookAll { }` | `method { }` / `constructor { }` 结果列表全挂 |
| `firstField { name = "..." }.of(instance).get<T>()` / `.set(v)` | 字段读写 |
| `dexKitBridge.findClass/findMethod/findField { matcher { ... } }` | DexKit 匹配（`.single()` / `checkDataList`） |
| `VariousClass(a, b, c).toClass()` | 多版本类名（`toClassOrNull` 可空变体） |
| `prefs(ModulePrefs).getBoolean(key, false)` | 开关读取 |
| `dataChannel.wait<T>(key) { v -> }` | 监听键值变化（`watch(key) { }` 用于块内自行重读 prefs 的场景） |
| `XLog.d/e(msg, t = throwable, tag = "...")` | 日志（自动走框架日志，回落 logcat） |
| `injectModuleAppResources()` | 把模块 APK 挂进宿主 AssetManager（见[资源注入](/dev/resources)） |

## 新增一个功能的完整步骤

1. **建 scope**：在 `hook/scopes/<group>/Xxx.kt` 实现 `Hooker`（按上面 DSL）
2. **挂路由**：在对应 `hook/hookers/Hook<App>.kt` 的 `onHook()` 内按开关 `loadHooker(Xxx)`
3. **加开关 UI**：在 `ui/fragment/scopes/` 对应页面加 `SwitchPreference`（key 与 `prefs` 读取一致，走 `ModulePrefs` 组）
4. **验证**：`./gradlew.bat :app:compileDebugKotlin` 通过 @ 真机开关跟随生效

## 系统框架 Hook

- 放 `hook/scopes/android/`，由 `HookAndroid`（`HookRouter.system` 注册）装载
- `HookCall.appClassLoader` 在 system_server 为 framework CL；不要假设存在 Application
- 需要跨 App 通用的配置逻辑放进 `hook/globals`

## 注意事项

- `before/after` 内**无参 `toClass()`** 使用注册快照的 loader（异步回调安全）
- 索引字段修改前先判空（宿主版本间字段名漂移，用 `firstFieldOrNull` + `name { it.contains(...) }` 更稳）
- hook 块内引用 `context` 用 `Env.hostContext()`，不要假设 application 已创建（冷启动早期分发）