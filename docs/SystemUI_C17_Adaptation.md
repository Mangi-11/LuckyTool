# SystemUI C17 (ColorOS 17) 适配分析

> 宿主 dump：`E:\DNA-WIN-1217\sysui17`（SystemUI）、`E:\DNA-WIN-1217\framework\c17`（framework）
> 扫描范围：`hook/scopes/systemui/` 81 个 hooker + `hook/utils` 辅助类，282 处类引用与 dump 逐一比对，再按 osCode=40（C17）加载门控过滤。

## 1. 移除状态栏支付保护图标（待适配）

- **现 hook 点**：`com.oplus.systemui.statusbar.phone.securepay.SecurePaymentControllerExImpl`（C12/C13）、`com.oplus.systemui.statusbar.phone.dynamic.SecurePaymentController`（C14）的 `handlePaymentDetectionMessage(Message)`，`intercept()`。
- **C17 现状**：上述两个类均不存在。消息处理迁移到 `com.oplus.systemui.common.manager.OplusSystemUiManagerExImpl`（私有方法，反编译可见 `access$handlePaymentDetectionMessage`）；图标渲染走新 pipeline 架构：
  - `com.oplus.systemui.statusbar.phone.dynamic.pipeline.data.securepayment.SecurePaymentRepository`
  - `.../pipeline/ui/viewmodel/SecurePaymentDynamicViewModel`
  - `.../dynamic/icon/SecurePaymentIconView`
- **适配方向**：拦截 `OplusSystemUiManagerExImpl` 的支付消息处理，或在新 pipeline 的图标显示处（ViewModel/IconView）阻断。

## 2. 移除GT模式通知（已按 C17 隐藏处理）

- **现 hook 点**：`GTUtils#notifyOpenGtMode`（C14+）/`showOpenGtModeNotify`（C13），类为 `com.oplusos.systemui.statusbar.util.GTUtils`（C13）/`com.oplus.systemui.statusbar.util.GTUtils`（C14）。
- **C17 现状**：dump 中无 GTUtils 类、无任何 GTMode 相关类与字符串，GT 模式通知在 C17 大概率已随 GT 模式改名/移除。
- **已处理**：`StatusBarNotify.kt` 加载门控 `osCode < 40`；`StatusBarNotifyRemoval.kt` 入口 `isVisible = getOSVersionCode < 40`。

## 3. 控制中心磁贴列数（已按 C17 隐藏处理）

- **现 hook 点**（`ControlCenterTilesColumn.kt`，对象名 `ControlCenterTiles`，由 `StatusBarTile.kt` 加载）：
  - `com.android.systemui.qs.QuickQSPanel#getNumQuickTiles`
  - `com.android.systemui.qs.TileLayout#updateMaxRows/updateColumns/setMaxColumns`（读写 mRows/mColumns 字段）
- **C17 现状**：两个类迁入 `com.android.systemui.qs.deprecated` 包；`getNumQuickTiles` 仍在，`TileLayout.updateMaxRows/setMaxColumns` 已不存在（只剩 0 参 `updateColumns()`）。
- **已处理**：`StatusBarTile.kt` 加载门控 `osCode < 40`；`StatusBarTiles.kt` 入口 `isVisible = osCode < 40`。
- **备注**：若未来要恢复 C17 支持，需按 `qs.deprecated` 新路径 + `updateColumns()` 新签名适配。

## 4. 电池信息通知（无需适配）

- `StatusBarBatteryInfoNotify` 依赖两个辅助类，均已核实存在：
  - `com.oplusos.systemui.common.battery.OplusBatteryController` ✓ 在 sysui17 dump 中
  - `vendor.oplus.hardware.charger.ICharger` ✓ 在 **framework/c17** dump 中（`vendor/oplus/hardware/charger/ICharger.java`：`Stub extends Binder implements ICharger`，`queryChargeInfo()` 接口声明 + Stub 实现齐全）。注意该类不在 SystemUI dex 内，属 framework 类。
  - 服务名 `vendor.oplus.hardware.charger.ICharger/default` 为运行时注册，静态无法核验，沿用 C14/C15 行为假设不变。

## 5. C17 上按门控不加载的功能（无需处理）

| 功能 | 门控 |
|---|---|
| 允许长按通知可修改 AllowLongPressNotificationModifiable | osCode ≤ 30 |
| 自定义流体云图标背景透明度 | osCode 30..33 |
| 移除控制中心多用户 | osCode < 26 |
| 移除锁屏关闭通知按钮 | osCode < 33 |
| 状态栏Feature（HookStatusBarFeature） | osCode < 34 |

## 6. 风险提醒

C17 SystemUI 大重构：`com.android.systemui.qs.*` 旧类整体迁入 `qs/deprecated`，新增 `com.oplusos.systemui` 与无前缀 `oplusos.systemui.keyguard` 命名空间。类级命中不等于方法级安全，**DexKit 结构匹配型与 `VariousClass` 多分支型 hook 建议 C17 真机回归一遍**。
