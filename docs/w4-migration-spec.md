# W4 批量迁移规范（YukiHookAPI → 原生 DSL）

> 供迁移 hooker 分组用。DSL 已就绪，本规范覆盖全部 legacy 用法。
> 每完成一个分组：路由注册 + YukiEntry 删除对应行 + `./gradlew.bat :app:compileDebugKotlin` 通过。

## 一、必改三件套（每个文件）

1. **基类**：`object X : YukiBaseHooker()` / `class X(...) : YukiBaseHooker()` → `: Hooker`（接口，无括号）
2. **删除 import**：`com.highcapable.yukihookapi.hook.entity.YukiBaseHooker`
3. **加 import**（按文件实际用法选）：
   - `com.luckyzyx.luckytool.hook.core.Hooker`（必加）
   - `com.luckyzyx.luckytool.hook.core.hook`（用了 `.hook {`）
   - `com.luckyzyx.luckytool.hook.core.hookAll`（用了 `.hookAll {`，含 constructor {} 列表形式）
   - `com.luckyzyx.luckytool.hook.core.get`（用了 `get<T>()` 带类型参数取字段）
   - `com.luckyzyx.luckytool.hook.core.result`（用了 `result<T>()`）
   - `com.luckyzyx.luckytool.hook.core.instance`（用了 `instance<T>()`）
   - `com.luckyzyx.luckytool.hook.core.injectModuleAppResources`（用了 `injectModuleAppResources`）

## 二、legacy 由 YukiBaseHooker 成员扩展提供的符号（迁移后需显式 import）

| 符号 | 迁移后 import |
|---|---|
| `"x".toClass(...)` / `cls.toClass(...)` | `com.highcapable.kavaref.extension.toClass` |
| `VariousClass(...).toClass()` / `VariousClass(...).toClassOrNull(...)` | `com.luckyzyx.luckytool.hook.core.toClass` / `core.toClassOrNull`（core 扩展，委托 load()/loadOrNull()） |
| `"x".toClassOrNull()` | `com.highcapable.kavaref.extension.toClassOrNull` |
| `.hook { }` | `com.luckyzyx.luckytool.hook.core.hook` |
| `.hookAll { }` | `com.luckyzyx.luckytool.hook.core.hookAll` |
| `FieldResolver.get<T>()` | `com.luckyzyx.luckytool.hook.core.get` |
| `HookParam.result<T>()` | `com.luckyzyx.luckytool.hook.core.result` |
| `HookParam.instance<T>()` | `com.luckyzyx.luckytool.hook.core.instance` |
| `YukiMemberHookCreator.MemberHookCreator` 接收者（hook {} 块内调用自定义扩展） | 改为 `com.luckyzyx.luckytool.hook.core.HookAction`（提供同形 before/after 等） |

KavaRef 原生（无需 import 迁移）：`of(instance)`、`set(value)`、`get()` 裸调用、`invoke(...)`、`firstMethod/firstMethodOrNull/firstField/firstFieldOrNull/firstConstructor/method{}/field{}/constructor{}`、`asResolver()`、`resolve()`、`classOf<T>()`、`optional(true)`。

## 三、行为等价点（DSL 已覆盖，照写不改）

- `hook { before { } after { } }`、`replaceTo(value)/replaceToTrue()/replaceToFalse()/intercept()`、`resultTrue()/resultFalse()/resultNull()`
- `args()`/`args(0)`/`args().first().string()`/`args().last().cast<T>()`/`args(0).any()`
- `result = x`、`throwable = t`、`hasThrowable()`
- `hook { }` 中 `instance`（HookCall 成员）、`prefs(...)` 若出现同样可用
- `.hook(priority = N) { }` 带优先级
- `method { }.hookAll`、`constructor { }.hookAll`、`firstMethodOrNull { }?.hook`

## 四、专项替换

- **YLog**：`import com.highcapable.yukihookapi.hook.log.YLog` → `import com.luckyzyx.luckytool.hook.core.XLog`，调用 `YLog.debug/error(msg, tag = ...)` → `XLog.debug/error(...)`（同形签名）
- **dataChannel**：`dataChannel.wait<T>(key) { x = it }`（带类型参数）**原样保留**（Hooker 提供 dataChannel 成员，等价远程偏好 + 监听）。若 legacy 文件没有显式 import dataChannel 相关符号，删掉多余 import。
  - **无类型参数且块内忽略推送值、自行重读 prefs 的 wait**（如 `dataChannel.wait("key") { val new = prefs(...).getStringSet(...) ... }`）→ 改为 `dataChannel.watch("key") { ... }`（去掉 lambda 参数，块体不变）
- **appInfo**：`appInfo.sourceDir` 等**原样**（Hooker.appInfo 非空）
- **DexKit**：`dexKitBridge.findClass/findMethod/findField`、`checkDataList`、`single()` 全部原样
- **`onAppLifecycle { }` 块**：**不要迁移该文件**，标记跳过交回主线（只有 HookSystemUIAutoStart、StatusBarBatteryInfoNotify 两个文件）

## 五、分组迁移清单（每组的 router + scopes 一起迁）

1. 读 legacy router（hook/hookers/HookXxx.kt）与其 import 的 scopes（hook/scopes/xxx/*.kt）
2. 全部按上面规则迁移
3. router 中回填 globals loadHooker 行（见下表），globals 已迁移无需再动
4. `HookRouterInit.register()` 加注册（见路由器注释区），YukiEntry 删对应 loadApp 行 + 删对应 import
5. 编译验证：`cd D:/Android/LuckyTool && ./gradlew.bat :app:compileDebugKotlin 2>&1 | grep -E "^e:|BUILD" | head -30`

### globals 回填表

**只有下表列出的分组需要回填 globals，其余分组一律不加。** 注意：表里的 HookGesture/HookMultiApp/HookSafeCenter 等指的是 hookers/ 下各自的 App 分组路由（如 hookers/HookGesture.kt），**不是** SystemUI 的子路由（HookSystemUIGesture/HookSystemUIFingerPrint/HookSystemUiMiscellaneous/HookSystemUIDialog/HookSystemUILockScreen/HookSystemUIStatusBar）——SystemUI 子路由 legacy 从未加载 globals（globals 已在 HookSystemUIFeature 内加载）。

| 分组 | 回填 globals |
|---|---|
| HookAndroid | HookGlobalFeatureConfig、HookGlobalSystemProperties、HookGlobalPmsFeature、HookGlobalSystemConfig |
| HookSystemUI（HookSystemUIFeature 内） | HookGlobalFeatureConfig、HookGlobalSystemProperties、HookGlobalFeatureProvider(dexKitBridge) |
| HookBattery | HookGlobalFeatureProvider(dexKitBridge) |
| HookLauncher | HookGlobalFeatureConfig、HookGlobalFeatureProvider(dexKitBridge) |
| HookOplusGames | HookGlobalFeatureConfig、HookGlobalFeatureProvider(dexKitBridge) |
| HookSettings | HookGlobalFeatureConfig、HookGlobalSystemProperties、HookGlobalFeatureProvider(dexKitBridge) |
| HookPhone | HookGlobalFeatureConfig、HookGlobalSystemProperties |
| HookSoundRecorder | HookGlobalSystemProperties |
| HookNotificationManager / HookGesture / HookMultiApp / HookOplusCosa / HookOplusMMS / HookPermissionController / HookPhoneManager / HookSafeCenter / HookSmartSidebar | HookGlobalFeatureConfig |

### 多包注册（legacy loadApp(pkg1, pkg2) 形态）

`HookRouter.app("com.oppo.launcher", HookLauncher)` + `HookRouter.app("com.android.launcher", HookLauncher)` 各一行。

### YukiEntry 删除后的 import 清理

每删一个 loadApp 行，删 YukiEntry 顶部对应 `import ...hookers.HookXxx` 行。

## 六、迁移后已可用的同形成员（无需 import 或按此 import）

- `HookCall.args` 属性（原始参数数组，`args.indexOfFirst {}` 等集合操作）+ `args()`/`args(vararg)` 函数并存
- `HookCall.instance<T>()` 非空强转（legacy 语义）；可空取值用成员属性 `instance` / `instanceOrNull`
- `Args.set(value)` / `Args.setTrue()` / `Args.setFalse()` / `Args.setNull()` / `Args.array<T>()` / `Args.list<T>()`（YukiHookAPI ArgsModifyer 全量同形）
- `prefs(...).getAppVerInfo(pkg)`：迁移后 import 换为 `com.luckyzyx.luckytool.hook.core.getAppVerInfo`（utils 版 receiver 是 YukiHookPrefsBridge，宿主进程不可用）
- `prefs(...)` 返回 NonNullPrefs：getString/getStringSet 返回非空（legacy YukiHookPrefsBridge 语义），调用点无需改动

## 七、编译错误常见修法

- `unresolved reference: hook/hookAll/get/result/instance` → 按第二节补 import
- `unresolved reference: toClass` → 补 kavaref.extension.toClass
- `type mismatch`（appInfo）→ 不存在（非空），若出现说明文件用了 Env 侧 null 场景，回报
- `cannot infer type`（dataChannel.wait 无类型参数）→ 给调用点显式类型参数：`dataChannel.wait<String>("key") { ... }`，且确认变量类型与 prefs 存储类型一致；若 legacy 依赖 channel 传非 prefs 类型（Pair/ArrayList），回报主线
- `ambiguous` 重载冲突 → 回报主线
