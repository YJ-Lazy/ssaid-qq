# Changelog

所有重要变更记录在此文件。

## [1.1.0] - 2026-08-24

### Added
- 针对 QQ NT 的 View 文本页面识别与 DecorView Overlay 状态显示。
- 新增只读 `ConfigProvider`，供 QQ 进程读取模块 APP 中保存的 SSAID。
- 新增 `scope.list`，默认作用域为 `com.tencent.mobileqq`。

### Changed
- 统一使用 libxposed API 102。
- Java 编译级别升级为 Java 17。
- 配置读取由 RemotePreferences 思路改为 ContentProvider。
- README 与当前实现保持一致。

### Fixed
- 修复 APP 内已配置 SSAID，但 QQ 进程读取为“未配置”的问题。
- 修复 QQ NT 关于页面依赖固定 Activity 类名导致的兼容问题。
- 修复早期 API102 Hook 回调写法导致的编译错误。

## [1.0.0] - 2026-08-24

### Added
- QQ 专用 SSAID Hook 初始版本。
- 一键生成 16 位十六进制 SSAID。
- LSPosed / libxposed API 102 模块入口。
- QQ 关于页面状态显示。
