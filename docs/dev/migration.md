# 迁移记录（YukiHookAPI → libxposed）

LuckyTool 历史上基于 YukiHookAPI 1.3.2（KavaRef + DexKit + de.robv 桥接）。为获得原生 libxposed 生态（CorePatch 上游、远程偏好、多入口），全量迁移到 `io.github.libxposed:api:102`，并**彻底删除 YukiHookAPI 依赖**。

## 迁移总览

| 阶段 | 内容 |
|---|---|
| W0 清场 | 删除 shim 模块、YukiHookAPI 接线、KSP 注解处理 |
| W1 基建 | 自建同形 Hook DSL（`hook/core`）+ `LibXposedEntry` + 试点迁移 |
| W2 核心补丁 | 集成上游 libxposed CorePatch + DisableFlagSecure |
| W3 globals | 8 个系统框架通用 Hooker 原生化 |
| W4 路由层 | `hook/hookers` 全量迁移（按 YukiEntry 顺序分批）+ `HookRouterInit` 注册表 + 删除 `YukiEntry` |
| W5 scopes | 289 个 `hook/scopes` 功能文件批量迁移（import/基类替换 + DSL 等价） |
| W6 模块侧 | UI/Service 去 YukiHookAPI（数据层切 remote prefs） |
| W7 收尾 | R8 验证、移除依赖、回归 |

## 迁移映射（速查）

| legacy 写法 | 原生等价 |
|---|---|
| `YukiBaseHooker` | `Hooker` |
| `loadApp(pkg, X)` / `loadSystem(X)` | `HookRouter.app(pkg, X)` / `HookRouter.system(X)` |
| `loadHooker(X)` | `Hooker.loadHooker(X)` |
| `.hook { }` | `hook { }`（同形扩展，内部 `XposedInterface.hook().intercept`） |
| `before/after` + `setResult/setThrowable` | `before/after` + `result=` / `throwable=` |
| `args(...).cast<T>()/set(v)` | 同形（`Args` 补全 `setTrue/setFalse/setNull/array/list`） |
| `instance` / `instance<T>()` | 同形（`instance` 非空 + `instanceOrNull`） |
| `"x".toClass()`（默认 loader） | kavaref 扩展；loader 缺省走 `ClassLoaderProvider`（由 `Env.enter` 统一） |
| `VariousClass(..).toClass()` | `hook.core.toClass` 扩展（委托 `load()`，返回 `Class<Any>` 保持与 `.of()` 兼容） |
| `YLog.*` | `XLog.*`（第二个位置参数是 `t: Throwable?`） |
| `prefs(ModulePrefs)` | 同形 → `Env.prefs`；返回 `NonNullPrefs`（getter 非空 + legacy 默认值） |
| `getAppVerInfo(pkg)` | `hook.core.getAppVerInfo`（原 utils 版 receiver 是 YukiHookPrefsBridge） |
| `injectModuleAppResources` | 同形 → `AssetManager.addAssetPath(模块 sourceDir)` |
| `dataChannel.wait<T>(key)` | 同形（remote prefs 等价物） |
| `onAppLifecycle { }` | `hook.core.onAppLifecycle`（`Application#onCreate` 兜底） |
| `YukiMemberHookCreator.MemberHookCreator` 接收者 | `HookAction` |

## 关键修正记录

- **`isFirstPackage` 门禁**：`onPackageReady` 只在每进程首次回调分发，根治共享进程多包回调对全局上下文的覆盖
- **ClassLoader 链顺序**：`分发 CL → currentApplication CL`。初始实现顺序颠倒导致 system_server 的所有无参 `toClass()` CNFE
- **PackageContext**：门禁落地后 Env 每进程只写一次，per-hooker 上下文层冗余被移除
- **指纹图标宿主适配**：C16 把 fade 动画内联进 R8 合成 Runnable（`updateOpticalUI` case21/22），静态写点全部取 `imMobileDrawable` 字段——适配方式从 hook 方法改为注入宿主原生字段 + 抗 SRC_ATOP 染色

## 现状

- 代码库 **0 处 YukiHookAPI 依赖**（核心 DSL 的文件注释除外）
- 注册文件三件套：`java_init.list`（3 入口）/ `module.prop` / `scope.list`（67 项含 system）
- 编译门禁：`compileDebugKotlin` 为日常基线；R8（`assembleRelease`）验证混淆链路