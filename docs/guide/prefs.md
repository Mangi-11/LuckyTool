# 配置说明

## 开关实时生效

所有功能开关通过 **LSPosed remote prefs** 同步：

```text
┌────────────────────┐        remote prefs        ┌─────────────────────┐
│  LuckyTool App (UI) │ ──────────────────────────► │ 宿主进程 (SystemUI 等) │
│  RemotePreferenceDS │   LSPosed XposedService     │  Env.prefs(...)     │
└────────────────────┘                              └─────────────────────┘
```

- UI 修改开关 → 写入模块侧 remote prefs
- 宿主进程内 `Env.prefs()` / `dataChannel.wait()` 读取同一数据源
- 无需重启即可生效（框架按需刷新偏好快照）

## 配置分组

| 组名 | 内容 |
|---|---|
| `ModulePrefs` | 功能开关主组（绝大多数功能） |
| `SettingsPrefs` | 通用设置（磁贴、总开关等） |
| `IntentPrefs` | 意图类配置 |
| `OtherPrefs` | 其它杂项 |

## 全局开关

| 开关 | 位置 | 说明 |
|---|---|---|
| 模块总开关 | App 主页 | `is_su`，控制所有宿主进程是否分发 Hook |
| `/sdcard/disable_lt` | 文件 | 存在即全局禁用（应急排查用） |

## 模块内 UI

- **功能页**：按作用域（系统界面 / 桌面 / 设置 / 系统应用 / 其他 App）分组呈现，每项带摘要与警告
- **磁贴（QuickSettings）**：全局 DC、高亮度模式可在快捷设置中直接切换
- **重启菜单**：部分功能启用后提供「重启 SystemUI / 系统界面」快捷入口