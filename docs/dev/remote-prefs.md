# 远程偏好

## 数据流

```text
┌─────────────────────────────────┐        LSPosed Binder        ┌────────────────────────┐
│  模块 App 进程（UI）              │  XposedService.getRemotePrefs │  宿主进程（SystemUI 等） │
│  LxServiceBridge.preferences()   │ ◄──────────────────────────► │  Env.prefs(name)        │
│  RemotePreferenceDataStore (UI) │                              │  Channel.wait/watch      │
│  SPUtils.appPrefs()             │                              │                         │
└─────────────────────────────────┘                              └─────────────────────────┘
```

**同一数据源、两个入口**：

- **宿主侧读**：`Env.prefs(name)` → `XposedInterface.getRemotePreferences(name)`（框架按 group 快照下发）。无 `PROP_CAP_REMOTE` 时打一次告警并回落 `EmptyPrefs`（全默认值）
- **UI 侧读写**：`LxServiceBridge` 经 `XposedServiceHelper` 绑定框架 service，`getRemotePreferences(name)` 返回 `RemotePreferences`
- **未绑定时回落**：UI 侧 `appPrefs()` 每个 get/put 都有本地 SharedPreferences fallback（无框架环境也能运行 UI）

## UI 侧接入

```kotlin
// Application.onCreate
LxServiceBridge.init()                       // 注册 OnServiceListener

// 激活判定（模块 App 启动时）
LxServiceBridge.awaitReady(3000)
if (!LxServiceBridge.isModuleActive) { ... } // scope 为空 = 未激活

// 读写（SPUtils 全量走这条）
context.appPrefs(ModulePrefs).getBoolean(key, false)
```

Preference UI 通过 `RemotePreferenceDataStore`（继承 androidx `PreferenceDataStore`）把整套 Switch/List 控件接到 remote store，`BaseScopePreferenceFeagment` 统一接线。

## 实时推送

宿主侧的 **`Channel.wait<T>(key)`** 是 legacy `YukiHookDataChannel` 的等价物：

```kotlin
dataChannel.wait<Boolean>("hide_nosim_noservice") { hideNoSS = it }
```

实现 = remote prefs 注册时补读一次当前值 + prefs 变更监听触发。**键不在 prefs 时不回调**（与 legacy「只在 push 时回调」一致）；块内自行重读 prefs 的无类型 wait 改用 `watch(key) { ... }`。

## 磁贴宿主侧链路

`GlobalDCTile` / `HighBrightnessModeTile` 点击 → 写 prefs → SystemUI 内 `HookSystemUIAutoStart` 订阅对应 key → 唤起 `AutoStartControllerService` 执行系统命令。旧 legacy 里数据是空推（无订阅者），原生版把发送—接收真正闭环。