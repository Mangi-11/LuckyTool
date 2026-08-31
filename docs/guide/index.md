# 简介

LuckyTool 是一款面向 **ColorOS / OPPO（一加）** 设备的 Xposed 模块，运行于 [LSPosed](https://github.com/mywalkb/LSPosed_mod) 框架之上，提供数十个系统级功能增强。

## 特性

- **范围广**：覆盖系统框架（`android`）、SystemUI、桌面、设置以及 40+ 个系统应用
- **原生 libxposed**：基于官方现代 API（libxposed 101/102）开发，不依赖任何桥接层
- **统一配置**：所有功能开关通过「远程偏好」读写，UI 修改立即同步到宿主进程
- **宿主维度适配**：按 ColorOS 版本（C12 ~ C16）自动匹配不同的 Hook 实现

## 支持环境

| 项目 | 要求 |
|---|---|
| 系统 | ColorOS / OPPO / 一加（部分功能通用 AOSP） |
| 系统版本 | Android 13 ~ 16（ColorOS C12 ~ C16） |
| 框架 | LSPosed（libxposed API ≥ 101） |
| 权限 | Root（部分功能强依赖，总开关检查） |

## 免责声明

本模块会修改系统行为，请在使用前充分了解每个功能的作用。由于系统版本差异，部分功能在新版系统上可能失效或不完全生效，遇到问题欢迎反馈。