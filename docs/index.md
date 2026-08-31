---
layout: home

hero:
  name: LuckyTool
  text: ColorOS / OPPO 系统增强模块
  tagline: 基于 LSPosed (libxposed) ，覆盖系统框架、SystemUI、桌面与数十个系统应用的功能增强。
  actions:
    - theme: brand
      text: 开始使用
      link: /guide/
    - theme: alt
      text: 开发者文档
      link: /dev/

features:
  - icon: 🧰
    title: 系统框架增强
    details: 个性化音量阶数、强制分屏、32 位应用支持、电池优化白名单、App 启动管理、风险应用拦截等框架级能力。
  - icon: 🎨
    title: SystemUI 深度定制
    details: 状态栏时钟/网速/图标细分、控制中心磁贴列数与透明度、锁屏时钟与充电组件、指纹图标替换与按压动画控制。
  - icon: 🏠
    title: 桌面增强
    details: Dock 背景与模糊、网格行列自定义、文件夹命名限制解除、任务卡片管理、应用角标与绿色更新点移除。
  - icon: ⚙️
    title: 设置与系统应用
    details: DPI 修改免重启、暗色模式自定义列表、应用语言、停用系统应用、自动填充、设备分享参数等 40+ 应用增强。
  - icon: 🔌
    title: 远程偏好
    details: UI 开关经 LSPosed remote prefs 直达宿主进程，实时生效，无需重启即可更新 Hook 配置。
  - icon: 🧩
    title: 原生 libxposed
    details: 100% 原生 libxposed API 102 实现（无 YukiHookAPI 桥接），自建同形 Hook DSL，一个 APK 覆盖全部宿主。
---