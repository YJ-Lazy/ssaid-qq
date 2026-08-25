# QQSSaidHook

QQ / QQ NT 专用的 LSPosed 模块，基于 **libxposed API 102**。

用于为 `com.tencent.mobileqq` 拦截 `Settings.Secure.ANDROID_ID`（SSAID），并在 QQ「关于」相关页面显示当前模块读取到的配置值。

> 本项目仅供 Android 兼容性学习与个人研究使用，与腾讯及 QQ 官方不存在隶属、合作或认可关系。

## 功能

- 仅作用于 QQ：`com.tencent.mobileqq`
- 基于 libxposed API 102，不混用旧 Xposed API
- APP 内一键生成 16 位十六进制 SSAID
- 使用只读 `ContentProvider` 向 QQ 进程提供当前配置
- Hook `Settings.Secure.getString(ContentResolver, String)` 的 `ANDROID_ID`
- 针对 QQ NT，通过 Activity 生命周期、TextView 文本及 View 无障碍描述识别「关于 QQ」页面
- 在相关页面提供独立的 SSAID 状态提示

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

模块 APP 将配置保存在自身 `SharedPreferences` 中，QQ 进程通过只读 `ContentProvider` 查询当前配置：

```text
content://com.example.ssaidhookQQ.config/config
```

### QQ NT 页面状态显示

当前实现不依赖固定 `AboutActivity` 类名，也不会仅凭普通设置页的 Activity 名称显示提示。页面恢复或获得焦点后，通过界面文本及无障碍描述识别「关于 QQ / About QQ」或 QQ 标识与版本信息的组合，并显示相应状态提示。

状态提示展示模块通过 `ContentProvider` 读取到的当前配置值；没有配置或读取失败时显示「未配置」。配置功能与页面提示相互独立，提示未显示不代表配置功能一定没有生效。

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
- QQ NT 界面更新可能影响页面识别和状态提示显示。
- 部分未提供可识别文本或无障碍描述的页面，可能无法显示状态提示。
- 配置 Provider 为只读 exported Provider，第三方应用理论上也可读取当前配置值。

## 商标与声明

QQ、腾讯及相关名称、标识和商标均归各自权利人所有。本项目为独立个人研究项目，不代表相关权利人的立场，也未获得其授权、赞助或认可。

## License

MIT License，见 [LICENSE](LICENSE)。
