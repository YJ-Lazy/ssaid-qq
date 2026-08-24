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

`1.1` (`versionCode 2`)

## 环境

| 项目 | 要求 |
| --- | --- |
| Android | 8.0+ / API 26+ |
| LSPosed | 支持 libxposed API 102 |
| Java | 17 |
| Android Gradle Plugin | 8.2.2 |
| compileSdk / targetSdk | 34 |
| 目标应用 | QQ `com.tencent.mobileqq` |

## 使用方法

1. 使用 Android Studio 打开项目并构建 APK。
2. 安装 APK。
3. 在 LSPosed 中启用 `QQSSaidHook`，作用域勾选 QQ。
4. 打开模块 APP，生成新的 SSAID。
5. 强制停止 QQ 后重新启动。
6. 进入 QQ 的「设置 → 关于 QQ」检查页面底部显示的 SSAID。

> Android 对后台进程管理有限制，模块 APP 的“结束 QQ 后台进程”不保证在所有 ROM 上都能真正强停 QQ。必要时请在系统设置中手动强制停止 QQ。

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

当前 API 配置：

```text
minApiVersion=102
targetApiVersion=102
staticScope=true
```

### SSAID Hook

核心 Hook 点：

```java
Settings.Secure.getString(ContentResolver, String)
```

当第二个参数为：

```java
Settings.Secure.ANDROID_ID
```

模块会读取配置并返回自定义 SSAID；没有配置或读取失败时调用原方法。

### 配置同步

模块 APP 将配置保存在自身 `SharedPreferences` 中：

```text
qqssaid_config
```

QQ 进程通过只读 `ContentProvider` 查询当前配置：

```text
content://com.example.ssaidhookQQ.config/config
```

Provider 不支持 `insert` / `update` / `delete`，仅用于读取模块配置。

### QQ NT 页面状态显示

当前实现不依赖固定的 `AboutActivity` 类名，而是在 QQ Activity 恢复/获得焦点后扫描页面 View 文本，通过「关于QQ / About QQ / 版本信息」等特征判断页面，并在 DecorView 上添加状态 Overlay。

QQ 更新后页面结构可能改变，因此这一功能的兼容性不保证永久稳定；SSAID Hook 与状态 Overlay 是两个独立部分。

## 构建

推荐 Android Studio + JDK 17。

Android Studio 中执行：

```text
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

默认 Debug APK：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 发布建议

建议 GitHub Release 使用版本标签：

```text
v1.1.0
```

Release 标题：

```text
QQSSaidHook v1.1.0
```

发布 APK 前请使用自己的签名构建 Release 版本，不要把 keystore、密码或 `keystore.properties` 提交到仓库。

## 已知限制

- 只处理 Java 层 `Settings.Secure.getString()` 的 `ANDROID_ID` 获取路径。
- QQ 自身若使用其他设备标识获取路径，不属于当前 Hook 范围。
- QQ NT UI 更新可能导致「关于 QQ」状态 Overlay 无法识别页面。
- 配置 Provider 必须允许 QQ 进程读取，因此当前 Provider 为 exported；它是只读接口，但第三方应用理论上也可查询该配置值。

## 致谢

本项目基于 / 参考 [YJ-Lazy/SSaidHook](https://github.com/YJ-Lazy/SSaidHook) 的思路进行 QQ 专用改造。

## License

MIT License，见 [LICENSE](LICENSE)。
