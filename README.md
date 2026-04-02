# MineBBS 自动更新工具

受到 [Sbaoor 老师的 Github Action](https://www.minebbs.com/resources/minebbs.15907/) 有感而生的 Gradle 插件。

## 兼容

- Java API 严格等级为 1.8;
- Gradle 测试标准为 7.4；
- 可能可以在更低的版本使用。

## 功能

1. 文件模式：
    文件模式下，会自动构建打包并推送成品文件。
2. Url 模式：
    Url 模式下会推送您的下载地址。

无论以何种模式，都会覆盖原有的下载方式（如果不同的话）。Url 模式并不会提取您 Url 中的文件，而是会要求用户跳转下载。

## 使用

### 导入
setting.gradle(.kts) 导入依赖项：

```kotlin
pluginManagement {
    repositories {
        maven {
            name = "kTT MavenReleases"
            url = uri("https://maven.kessokuteatime.work/releases")
        }
    }
}
```

然后使用插件：

```kotlin
plugins {
  `minebbs-publisher`
}
```

### 配置

开始之前，您需要先持有 “开发者” 身份。[这是什么？](https://www.minebbs.com/threads/minebbs.4085/)

如果您想更新作品，您就得先发布作品。在您的作品地址中取得您的作品 ID。
示例：`https://www.minebbs.com/resources/lib-wolftailui-gui.12001/` 的 ID 为 12001。 

随后，您需要在 MB 开放平台中[申请 API Token](https://api.minebbs.com/#/)

现在配置插件：

```kotlin
publisherMineBBS {
  token = "******"                              // 这里需要您刚才取得的 API Token
  projectId = 114514                            // 作品 ID
  version = project.version                     // 更新后的版本，通常留作项目版本即可。
  title = "Yes 册那！${project.version} 更新！"  // 日志标题。
  description = "修复了狼没有凤梨的 Bug。"        // 日志正文。
  url = "https://114.514.1919.810/"             // [可选] 更新后的 Url。此项如不填或留空，则进入文件模式。会自动编译与上传文件。
}
```