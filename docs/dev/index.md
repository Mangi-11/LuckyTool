# 开发者总览

LuckyTool 是全量原生化 **libxposed**（Modern Xposed API）的 Xposed 模块。历史上从 YukiHookAPI 迁移而来（见[迁移记录](/dev/migration)），当前代码库不包含任何 YukiHookAPI / de.robv 桥接依赖。

## 高层架构

```text
┌─────────────────────────────── LibXposedEntry ───────────────────────────────┐
│  onModuleLoaded      → Env.attach(框架接口 + 模块路径) + HookRouterInit.register()  │
│  onPackageReady      → isFirstPackage 门禁 → HookRouter.dispatch(pkg)           │
│  onSystemServerStarting → HookRouter.dispatch("android")                        │
└───────────────────────────────────────────────────────────────────────────────┘
                                      │
              ┌───────────────────────┼───────────────────────────┐
              ▼                       ▼                           ▼
        hook/hookers               hook/scopes                hook/globals
        （每 App 路由）             （功能实现）                  （系统框架通用）
              └──────────────── HookRouter / Env / Hooker DSL ──┘
```

## 模块组成

| 目录 | 职责 |
|---|---|
| `hook/core` | 自建同形 Hook DSL（`Hooker`/`HookAction`/`HookCall`/`Channel`/`XLog`/`Env`） |
| `hook/hookers` | 各宿主 App 的路由 Hooker（约 60 个） |
| `hook/scopes` | 功能实现（约 280 个 scope 类） |
| `hook/globals` | system_server 内跨 App 通用 Hook（FeatureConfig/SystemProperties 等） |
| `hook/utils` | 宿主侧工具（DexKit 匹配辅助、ReflectKit、sysui 工具） |
| `org/lsposed/corepatch` | 上游 CorePatch（核心破解组件，libxposed 版） |
| `io/github/lsposed/disableflagsecure` | 上游 DisableFlagSecure（禁用 FlagSecure，libxposed 版） |
| `ui/` | 模块 App 本体（配置 UI + LxServiceBridge） |
| `service/` | TilesService / 磁贴 / 宿主进程内的寄生服务 |

## 依赖策略

| 依赖 | 用途 |
|---|---|
| `io.github.libxposed:api` (compileOnly) | 唯一的 Hook 引擎 |
| `io.github.libxposed:service` | 模块侧 remote prefs / scope 管理载体 |
| `com.highcapable.kavaref` | 纯反射查找 DSL（与框架无关，保留） |
| `org.luckypray.dexkit` | Dex 分析匹配（保留） |
| `org.lsposed.lsparanoid` | KSP 字符串混淆（保留） |
| `org.lsposed.hiddenapibypass` | 隐藏 API 访问 |

## 关键存根

- **`resources/META-INF/xposed/java_init.list`**：注册 3 个入口（LuckyTool + CorePatch + DisableFlagSecure），R8 会改写混淆类名
- **`scope.list`**：作用域名单（67 项，含 `system`）
- **模块资源包 id**：保留段 `0x64`（`--package-id 0x64`），与宿主 0x7f 不冲突