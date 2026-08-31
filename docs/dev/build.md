# 构建与发布

## 构建要求

- JDK 21（`kotlin { jvmToolchain }` 指定）
- Android SDK：compileSdk 由 `rootProject.extra` 注入
- 签名：`keystore.storeFile/storePassword/keyAlias/keyPassword` 在 `local.properties`/gradle 属性中配置

## 常用构建命令

```bash
# 日常验证（只编 Kotlin，最快）
./gradlew.bat :app:compileDebugKotlin

# Debug 包
./gradlew.bat :app:assembleDebug

# Release 包（R8 混淆 + 资源压缩）
./gradlew.bat :app:assembleRelease
```

::: tip
日常 Hook 改动只需要 `compileDebugKotlin` 通过即可；**涉及 R8 规则 / 依赖移除 / 资源改动**时才需要额外跑 `assembleRelease` 验证混淆链路。
:::

## 版本号

- `app/version.properties` 中 `versionCode` **每次构建自动自增**（Gradle 读取并回写）
- 输出文件名：`LuckyTool_v<versionName>(<versionCode>)_<buildType>.apk`

## 混淆与资源

- **lsparanoid**：KSP 字符串混淆（release variant），掩盖 hook 关键字与日志串
- **resopt**：资源对齐压缩
- **保留资源包 id**：`--allow-reserved-package-id --package-id 0x64`，模块资源 id 固定落在 0x64 段
- **R8 keep**：`proguard-rules.pro` 保留 libxposed 入口与 `java_init.list`（`-adaptresourcefilecontents`）

## 发布前检查

1. `compileDebugKotlin` + `assembleRelease` 均通过
2. 真机回归：入口日志（各宿主进程 `LibXposedEntry loaded`）→ 总开关 → 代表性功能开关跟随 → CorePatch / DisableFlagSecure → remote prefs 快照
3. 更新 `changelog`（App 内置使用）