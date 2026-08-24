# QQSSaidHook

QQ / QQ NT 专用的 LSPosed 模块，基于 **libxposed API 102**。

用于为 `com.tencent.mobileqq` 拦截 `Settings.Secure.ANDROID_ID`（SSAID），并在 QQ「关于」相关页面显示当前模块读取到的配置值。

> 本项目仅供 Android Hook / LSPosed 学习与个人研究使用。

## 功能

- 仅作用于 QQ：`com.tencent.mobileqq`
- 基于 libxposed API 102，不混用旧 Xposed API
- APP 内一键生成 16 位十六进制 SSAID
- 使用只读 `ContentProvider` 向 QQ 进程提供当前配置
- Hook `Settings.Secure.getString(ContentResolver, String)` 的 `ANDROID_ID`
- 针对 QQ NT，通过 Activity 生命周期 + View 文本识别「关于 QQ」页面
- 使用 DecorView Overlay 显示当前 SSAID，不修改 QQ 原页面布局

## 当前版本

`1.1.0` (`versionCode 2`)

## 环境

| 项目 | 要求 |
| --- | --- |
| Android | 8.1+ / API 27+ |
| LSPosed | 支持 libxposed API 102 |
| Java | 17 |
| Android Gradle Plugin | 8.7.3 |
| compileSdk / targetSdk | 35 |
| 目标应用 | QQ `com.tencent.mobileqq` |

## 使用方法

1. 使用 Android Studio 打开项目并构建 APK。
2. 安装 APK。
3. 在 LSPosed 中启用 `QQSSaidHook`，作用域勾选 QQ。
4. 打开模块 APP，生成新的 SSAID。
5. 强制停止 QQ 后重新启动。
6. 进入 QQ 的「设置 → 关于 QQ」检查页面底部显示的 SSAID。

## 项目结构

```text
QQSSaidHook/
├─ app/
│  ├─ build.gradle
│  └─ src/main/
│     ├─ AndroidManifest.xml
│     ├─ java/com/example/ssaidhookQQ/
│     │  ├─ App.java
│     │  ├─ ConfigProvider.java
│     │  ├─ MainActivity.java
│     │  └─ MainHook.java
│     ├─ res/
│     └─ resources/META-INF/xposed/
│        ├─ java_init.list
│        ├─ module.prop
│        └─ scope.list
├─ CHANGELOG.md
├─ LICENSE
├─ build.gradle
├─ gradle.properties
└─ settings.gradle
```

## 实现说明

### Xposed 入口

入口类：

```text
com.example.ssaidhookQQ.MainHook
```

模块声明位于：

```text
app/src/main/resources/META-INF/xposed/
```

### SSAID Hook

核心 Hook 点：

```java
Settings.Secure.getString(ContentResolver, String)
```

模块 APP 将配置保存在自身 `SharedPreferences` 中，QQ 进程通过只读 `ContentProvider` 查询当前配置：

```text
content://com.example.ssaidhookQQ.config/config
```

### QQ NT 页面状态显示

当前实现不依赖固定 `AboutActivity` 类名，而是在 QQ Activity 恢复/获得焦点后扫描页面 View 文本，通过「关于QQ / About QQ / 版本信息」等特征判断页面，并在 DecorView 上添加状态 Overlay。

## 构建

推荐 Android Studio + JDK 17。

```text
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

默认 Debug APK：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 已知限制

- 只处理 Java 层 `Settings.Secure.getString()` 的 `ANDROID_ID` 获取路径。
- QQ 自身若使用其他设备标识获取路径，不属于当前 Hook 范围。
- QQ NT UI 更新可能导致「关于 QQ」状态 Overlay 无法识别页面。
- 配置 Provider 为只读 exported Provider，第三方应用理论上也可读取当前配置值。

## License

MIT License，见 [LICENSE](LICENSE)。
