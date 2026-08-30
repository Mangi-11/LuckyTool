# LuckyTool libxposed 原生化迁移方案（全量替换版）

> **决策变更**：废弃上一版"保留 YukiHookAPI + de.robv shim 桥接"方案。
> 新方案 = **彻底移除 YukiHookAPI 全家桶**，hooker / 特性 / utils 全部迁移到 libxposed 原生 API
> （`io.github.libxposed:api:102.0.0`），同时自建一层**同形轻量 Hook DSL** 承接 375+ 个
> hooker，把迁移量从"逐点手写"压缩为"机械替换"。

---

## 一、现状迁移面（实测数据，决定本方案结构）

| 项 | 数量 | 说明 |
|---|---|---|
| 引用 `com.highcapable.yukihookapi` 的文件 | **391** | 全部需去依赖 |
| hook 创建点（`.hook {`） | **745** | 需映射到 `XposedInterface.hook().intercept` |
| before/after/replaceTo/intercept 块 | **789** | 链语义需等价实现 |
| firstMethod/firstField/firstConstructor 查找 | **1520** | KavaRef 原语，**保留不变** |
| loadApp/loadSystem/loadHooker 调用 | **533** | 需自建路由 |
| prefs() 读取 | **654** | 换 Env 远程偏好（同形函数可零改动） |
| YLog 输出 | **115** | 换 `XposedInterface.log` |
| DexkitUtils/dexKitBridge 引用 | 113 文件 | **保留**（DexKit 与框架无关，只去 YLog） |
| de.robv 直接调用 | 11 文件 | CorePatch(7)+DisableFlagSecure(1)+XposedHelper+MainHook/XposedEntry |
| UI 层 YukiHookAPI | 3 文件 | MyApplication(ModuleApplication) + BaseScopePreferenceFeagment/QuickEntryFragment(ModulePreferenceFragment) |

hook 侧文件分布：`hook/hookers` 67（每 App 一个路由 Hooker）、`hook/scopes` 289（功能实现）、
`hook/globals` 8（系统框架 DexKit 钩子）、`hook/CorePatch` 7、`hook/DisableFlagSecure` 1、`hook/statusbar` 11。

**结论**：直接手写替换 745 个 hook 点不可行（回归风险 × 时间成本都不成立），
必须自建 DSL。DSL 的形状对齐现有代码 → 大部分 hooker 的迁移是 import + 基类名替换级别的机械操作。

---

## 二、目标技术栈

| 组件 | 处置 |
|---|---|
| `io.github.libxposed:api:102.0.0` | compileOnly，唯一 hook 引擎 |
| `io.github.libxposed:service:102.0.0` | 模块 App 侧保留（远程偏好/scope 管理载体） |
| **KavaRef**（kavaref-*） | **保留**——纯反射 DSL，与框架无关，1520 处查找全部不动 |
| **DexKit 2.2.0** | **保留**——纯 dex 分析，与框架无关 |
| **hiddenapibypass 6.1** | **保留**——运行时隐藏 API 访问 |
| **lsparanoid**（KSP 字符串混淆） | **保留**——与框架无关 |
| YukiHookAPI / ksp-xposed / xposed-api(82) | **全部移除** |
| xposed-api-compat shim 模块 | **删除** |
| UI prefs 基类 | ModulePreferenceFragment → androidx PreferenceFragmentCompat |

---

## 三、新基建设计（先建基建，再搬功能）

### 3.1 运行环境持有器 `Env`

```kotlin
// com.luckyzyx.luckytool.hook.core.Env
object Env {
    lateinit var base: XposedInterface     // LibXposedEntry 注入
    var processName: String = ""
    var classLoader: ClassLoader? = null   // 当前宿主包 CL

    /** 远程偏好：宿主进程内读模块 App 的同名 SharedPreferences（快照语义） */
    fun prefs(name: String): SharedPreferences

    fun log(priority: Int, tag: String, msg: String, t: Throwable? = null)
}
```

KavaRef 的 `ClassLoaderProvider` 在进入每个包回调时切到 `Env.classLoader`，
使现有 `"cls".toClass()` 原封不动工作。

### 3.2 同形 Hook DSL（app 模块自建，`hook/core` 包）

```kotlin
interface Hooker {
    fun onHook(cl: ClassLoader)              // 对应 YukiBaseHooker.onHook
}

object HookRouter {                          // 对应 YukiEntry 的 loadApp/loadSystem
    fun app(packageName: String, processName: String? = null, hooker: Hooker)
    fun system(hooker: Hooker)               // packageName == "android"（system_server）
}

fun Method?.hook(priority: Int = 50, action: HookAction.() -> Unit)      // 对应 .hook { }
fun HookAction.before(block: HookCall.() -> Unit)
fun HookAction.after(block: HookCall.() -> Unit)
fun HookAction.replaceTo(result: Any?)                                    // 返回替换 + 跳过原方法
fun HookAction.intercept()                                                // 阻断原方法
class HookCall {
    val instance: Any?          // thisObject
    val args: Array<Any?>       // 可直接改写，与原 YukiHookAPI args() 同形
    var result: Any?
    var throwable: Throwable?
}
```

内部实现统一走 `Env.base.hook(executable).setPriority(p).setExceptionMode(PROTECTIVE).intercept { chain -> ... }`，
链语义按 legacy 惯例：before → （未提前返回则 proceed(修改后 args)，异常入 throwable）→ after → 抛 throwable/返回 result。

配套同形工具（让现有代码几乎零改动）：

```kotlin
// utils 层同形函数
fun prefs(name: String) = Env.prefs(name)          // prefs(ModulePrefs).getBoolean(...) 原样可用
object XLog { fun debug/i/warn/e(tag, msg, t) }    // 批量替换 YLog → XLog
```

### 3.3 入口与路由

`LibXposedEntry` 重写（保留阶段 2-3 已交付的注册文件三件套不变）：

```kotlin
class LibXposedEntry : XposedModule() {
    override fun onModuleLoaded(p) { Env.base = this; Env.processName = p.processName }
    override fun onPackageReady(p) { Env.classLoader = p.classLoader; HookRouter.dispatch(p.packageName, p.classLoader) }
    override fun onSystemServerStarting(p) { Env.classLoader = p.classLoader; HookRouter.dispatch("android", p.classLoader) }
}
```

`YukiEntry` 重写为 `HookRouter` 注册表（53 个 loadApp 平移，逻辑不变）。

### 3.4 删旧（已执行：shim 三连提交已从 libxposed 分支重置移除）

- ✅ 已删除：`:xposed-api-compat` 模块（含残留 build 目录）、app 的 shim/legacy 接线、
  `LibXposedEntry`、proguard shim keep 规则、MyApplication 的 XposedServiceHelper 绑定
- ✅ 分支已回到 `191c350e`（与 origin/main 同点），工作区仅剩本方案文档
- 待 W0-W1 执行：删除 `assets/xposed_init`、`resources/META-INF/yukihookapi_init`、
  `MainHook.kt`、`XposedEntry.kt`（逻辑并入路由）、KSP `ksp-xposed` 依赖与
  `@InjectYukiHookWithXposed`；`compileOnly(libs.xposed.api)` 待 CorePatch 迁移完成后删除
- 注册文件三件套（java_init.list/module.prop/scope.list）**随重置一并移除，W1 重建**：
  - java_init.list → 重写后的 LibXposedEntry
  - scope.list → 以 `@array/xposed_scope` 为源，**并补回 `system` 条目**
    （用户此前手工添加的框架作用域项，libxposed 下对应系统框架包，必须保留）

---

## 四、迁移映射表（YukiHookAPI → 新基建）

| 现有写法 | 迁移后 |
|---|---|
| `class X : YukiBaseHooker()` / `object X : YukiBaseHooker()` | `class X : Hooker`（签名 `onHook(cl: ClassLoader)`；`dexKitBridge` 构造参数保留） |
| `"cls".toClass().resolve().apply { ... }` | **原样**（KavaRef，ClassLoaderProvider 已切 Env） |
| `firstMethodOrNull { name = ...; parameterCount = N }?.hook { ... }` | 原样（KavaRef + 新 `Method.hook`） |
| `hook { if (cond) replaceToFalse() }` | `hook { if (cond) replaceTo(false) }` |
| `before { args().last().cast<String>() ... result = x }` | `before { args.last().cast<String>() ... result = x }` |
| `after { instance<...>()... }` | `after { instance... }` |
| `firstFieldOrNull { type = ... }?.set(value)` | KavaRef 字段原语 + 新 `get/set` 辅助 |
| `prefs(ModulePrefs)` | 同形函数 `prefs(ModulePrefs)`（Env 远程偏好） |
| `YLog.debug/error(...)` | `XLog.debug/error(...)`（批量替换） |
| `loadApp("pkg", HookX)` / `loadSystem(HookX)` / `loadHooker(HookX)` | `HookRouter.app("pkg", HookX)` / `system(HookX)`（loadHooker 直接实例化调用） |
| `dexKitBridge.findClass {...}.apply { checkDataList(...) }` | 原样（DexkitUtils 仅替换 YLog） |
| CorePatch 的 `XposedHelpers.* / XC_MethodHook / XposedBridge.*` | 收敛到改造后的 `XposedHelper.java`（唯一适配点）：反射走自建 ReflectUtils（可复用 KavaRef），hook 走 `Env.base.hook`，`deoptimizeMethod` 走 `Env.base.deoptimize`，prefs 走 `prefs("ModulePrefs")` |

---

## 五、分波次执行计划（每波独立 commit + 编译通过 + 真机抽查）

| 波次 | 内容 | 规模 | 里程碑 |
|---|---|---|---|
| **W0 清场** | 删 xposed-api-compat、去 YukiHookAPI 依赖接线（保留 libxposed api/service）、删 MainHook/XposedEntry/生成产物 | 20 文件 | 编译失败状态合法（旧 hooker 全部失联），随后 W1 恢复 |
| **W1 基建** | Env/HookAction/HookRouter/XLog/prefs 同形函数 + LibXposedEntry 重写 + 迁移 1 个试点（`HookBattery` 1-2 功能） | 8 新文件 + 试点 | 真机：试点功能生效 |
| **W2 核心补丁** | `XposedHelper.java` 改造 → CorePatch 7 文件 + DisableFlagSecure | 9 文件 | Android 15/16 重启不崩、核心补丁生效 |
| **W3 globals + statusbar** | hook/globals 8 + hook/statusbar 11 | 19 | 系统框架钩子生效 |
| **W4 路由层 hookers** | hook/hookers 67 个按 YukiEntry 顺序迁移（每个只改路由签名） | 67 | 全 App 分发生效 |
| **W5 scopes 功能层** | hook/scopes 289 个按 scope 分组批量迁移（同形 DSL 下多为 import/基类替换；每迁移一个 scope 编译一次） | 289 | 全功能迁移完 |
| **W6 UI 与 utils** | BaseScopePreferenceFeagment → PreferenceFragmentCompat、QuickEntryFragment、MyApplication 去 ModuleApplication、DexkitUtils 去 YLog | ~15 | 模块 App 独立运行无 YukiHookAPI 痕迹 |
| **W7 收尾** | 删 ksp-xposed/lsparanoid 保留、R8 规则复核、真机回归清单 | — | 发布候选 |

每波迁移顺序建议按 `YukiEntry` 里 loadApp 的声明顺序推进（先系统侧后三方 App），
便于边迁移边在真机回归。

---

## 六、验证与回滚

- **每波独立 commit**；main 分支保留 legacy 版 APK 可随时切回
- 真机清单（沿用）：入口 splash 日志 → `is_su` 远程偏好前提 → 任一开关开/关跟随 →
  CorePatch → release 混淆包 → 远程偏好快照新鲜度
- 风险预案：若某 scope 的 DSL 形状覆盖不了（特殊用法），允许该文件手写原生
  `Env.base.hook(...)` 长式写法，不阻塞波次

---

## 七、工作量估算

| 波次 | 估时 |
|---|---|
| W0-W1（清场+基建+试点） | 3d |
| W2（CorePatch） | 2d |
| W3 | 1.5d |
| W4 | 2d |
| W5（大头，289 文件） | 8-10d |
| W6 | 2d |
| W7（回归） | 2d |
| **合计** | **约 4-5 周**（对比已废弃 shim 方案约 1 周，换来零桥接层、零 legacy 依赖的纯原生代码库） |

> 本方案假设 DSL 形状覆盖现有全部用法（实测 745 hook 点中无超出映射表的构造）。
> W5 是唯一有风险集中区，按 scope 分组推进即把风险摊薄到可回滚粒度。
