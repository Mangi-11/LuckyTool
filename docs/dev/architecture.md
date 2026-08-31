# Hook 架构

## 分发流水线

```text
onPackageReady(pkg)
  ├─ if (!param.isFirstPackage) return   ← 每进程只分发一次，避免共享进程多包回调
  │                                       覆盖全局上下文（包名 / classLoader / Provider）
  └─ HookRouter.dispatch(pkg, cl, appInfo)
       └─ Env.enter(pkg, cl, appInfo)      ← 设置当前宿主上下文
            ├─ ClassLoaderProvider.classLoader = activeClassLoader()
            └─ 依次执行 hooker.onHook()  （异常隔离，逐个 try/catch）

onSystemServerStarting
  └─ HookRouter.dispatch("android", cl, null)   ← system_server 不分 isFirstPackage
```

## ClassLoader 解析链

`Env.activeClassLoader()` 顺序**对齐 legacy YukiHookAPI PackageParam**：

```kotlin
分发宿主包 CL（param.classLoader，wrapper.appClassLoader 等价）
  → 进程 Application CL（ActivityThread.currentApplication.classLoader）
```

关键约束：

- **system_server 不能退到 currentApplication** —— LSPosed 会为 system 进程挂一个 Application，其 CL 看不到 `com.android.server.*`（曾导致 24 个系统 Hook 全挂 CNFE）
- 配 `isFirstPackage` 门禁后，`Env` 全局在每进程内**只写一次、永不漂移**，不必再引入 per-hooker 上下文层（PackageContext 方案评估后被移除）

## HookCall 快照

每次 `.hook {}` 注册发生在宿主包 dispatch 的**同步期**（上下文必然正确），构造函数快照：

- `hookClassLoader`（= 注册时 `ClassLoaderProvider` 值）
- `hookPackageName`（= 注册时 `Env.packageName`）

`before/after` 回调里的 `packageName` / `appClassLoader` 优先读快照，兜底 `Env`——等价 legacy `HookParam` 的按包解析语义。

## 核心成员面

| 成员 | 语义 |
|---|---|
| `Hooker.onHook()` | 分发入口（等价 `YukiBaseHooker.onHook`） |
| `Hooker.packageName / processName / classLoader / appClassLoader / appInfo` | 宿主包元信息（终读 Env） |
| `Hooker.prefs(name)` | 远程偏好（NonNullPrefs 包装，非空 getter + legacy 默认值） |
| `Hooker.loadHooker(hooker)` | 子 Hooker 装载，异常隔离 |
| `Hooker.dataChannel` | 实时配置推送的 remote prefs 等价物（见[远程偏好](/dev/remote-prefs)） |
| `Hooker.onAppLifecycle { ... }` | 宿主 Application 就绪后执行（冷启动早期挂 `Application#onCreate` 等待） |

## 异常模型

- 路由层 `dispatch` / `loadHooker` 双层 `runCatching` 隔离——单个功能挂掉不影响其它功能
- Hook 链 `ExceptionMode.PROTECTIVE` + 链语义：`before → (未提前返回则 proceed 修改后 args) → after → 抛 throwable / 返回 result`