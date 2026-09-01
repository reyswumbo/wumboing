# 🚀 Wumboing

**Wumboing** — 一个现代的、轻量级的原生 Android 漫画阅读应用。一个应用、多个来源、统一阅读体验。

Wumboing 将两个漫画内容来源整合到同一个原生 Android 应用中，并通过导航抽屉（Navigation Drawer）一键切换来源，无需分别打开各自的网站。

---

## 📖 项目介绍

Wumboing 是一个使用 **Kotlin + Jetpack Compose + Material 3** 构建的原生 Android 漫画阅读应用。它聚合了以下两个漫画网站的内容：

| 网站 | 应用内名称 |
| ---- | ----------- |
| **Wurmz** (`https://wurmz.net/`) | **WZ** |
| **Alawale** (`https://alawale.net/`) | **AW** |

> 注意：在应用的用户界面（UI）中，只显示缩写名称 **WZ** 与 **AW**，绝不显示 "Wurmz" 或 "Alawale" 的全名。全名仅用于源代码与本文档的技术说明。

应用 UI 全程使用 **印尼语（Bahasa Indonesia）**，而本文档（README）使用中文撰写。

---

## ✨ Wumboing 的功能

- 🕹️ **多来源支持**：通过汉堡菜单（Navigation Drawer）在 **WZ** 与 **AW** 之间切换。
- 🔄 **来源相互独立**：单个来源故障不会影响另一个来源，应用会优雅地处理错误并显示印尼语错误信息。
- 🎨 **现代原生 UI**：基于 Material 3 的自有设计系统，专为移动端漫画阅读优化。
- 🌙 **暗色/亮色模式**：支持深浅主题切换。
- 📖 **漫画阅读器**：竖屏滚动阅读、平滑图片加载、加载/失败状态。
- 🔍 **搜索**：在每个来源中搜索漫画。
- 🏷️ **类型/分类**：显示漫画的体裁（Genre）与类型信息。
- 📚 **章节导航**：查看漫画全部章节列表。
- ⭐ **书签 / 收藏**：收藏喜欢的漫画。
- 📖 **阅读历史**：记录阅读历史，支持“继续阅读”。
- 🔔 轻量、快速、好用的阅读体验。

---

## 🛠️ 使用的技术

| 类别 | 技术 |
| ---- | ---- |
| 语言 | Kotlin |
| UI | Jetpack Compose、Material 3、Navigation Compose |
| 架构 | MVVM（ViewModel + Repository + Source 层）|
| 依赖注入 | Koin |
| 网络 | OkHttp |
| HTML 解析 | jsoup（Web Scraping）|
| 图片加载 | Coil |
| 本地存储 | DataStore（Preferences）|
| 序列化 | kotlinx.serialization |
| 构建 | Gradle + Android Gradle Plugin (AGP) |

---

## 📐 项目架构

项目采用清晰的 **MVVM + 分层架构**，每个来源相互独立、可替换：

```
┌─────────────────────────────────────────────┐
│                   UI 层 (Compose)           │
│   Home · Detail · Reader · Library          │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│               ViewModel 层                  │
│   HomeViewModel · DetailViewModel           │
│   ReaderViewModel · LibraryViewModel        │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│              Repository 层                  │
│          ComicRepository（编排多来源）       │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│              Source 层（来源）              │
│   ├── WZ  ── Wurzum 集成                    │
│   └── AW  ── Alawale 集成                   │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│            数据 / 本地存储                   │
│       LocalStore（书签 · 历史 · 主题）       │
└─────────────────────────────────────────────┘
```

| 源码目录 | 说明 |
| -------- | ---- |
| `data/model` | 数据模型（Comic、Chapter、ComicDetail 等）|
| `data/source` | 来源抽象与 WZ / AW 实现 |
| `data/repository` | 仓库层，调度多个来源 |
| `data/local` | 本地存储（书签、历史、主题）|
| `di` | Koin 依赖注入模块 |
| `ui/theme` | Material 3 主题 |
| `ui/home` | 首页（来源切换 + 漫画列表 + 搜索）|
| `ui/detail` | 漫画详情（章节列表 + 书签）|
| `ui/reader` | 漫画阅读器（竖屏滚动）|
| `ui/library` | 书签 / 阅读历史 |

---

## 🌐 数据来源与 Web Scraping 实现

### WZ（Wurmz，`https://wurmz.net/`）

Wurmz 是一个 Next.js（App Router）服务端渲染站点，所有内容都直接包含在返回的 HTML 中，因此可以稳定地抓取：

- **列表 / 首页**：`https://wurmz.net/`（最新更新）
- **全部漫画**：`/semua-komik?sort=new&page=N`
- **搜索**：`/search?q={keyword}`
- **漫画详情**：`/detail/{type}/{slug}`，其中 `{type}` 为 `manga` / `manhwa` / `manhua`
  - 通过页面内嵌的 `ComicSeries` JSON-LD 结构化数据解析标题、封面、作者、体裁、简介。
  - 通过页面内嵌的 `chapters` JSON 数组解析章节列表（`chapter_label`）。
- **章节阅读页**：`/detail/{type}/{slug}/chapter/{number}`
  - 通过正则从 HTML 中抽取 `https://bmcdn.my.id/...jpg` 的章节图片地址。

解析选择器：
- 漫画卡片：`article.comic-card`
- 标题：`.comic-title`
- 封面：`.cover-frame img`（`/covers/{type}__{slug}.webp`）
- 类型徽章：`.type-badge`
- 最新章节：`.ch-row .ch-num`

### AW（Alawale，`https://alawale.net/`）

Alawale 同样是 Next.js 服务端渲染站点，内容全部包含在 HTML 中：

- **列表 / 首页**：`https://alawale.net/`（最新更新、新漫画）
- **全部漫画**：`/daftar-komik?sort=update`
- **搜索**：`/daftar-komik?q={keyword}`
- **漫画详情**：`/{slug}`
  - 标题：`.detail-info h1`
  - 封面：`.detail-hero .cover img`
  - 体裁：`.detail-info .genres a.chip`
  - 简介：`.syn-body`
  - 章节：`.chap-list a.chap-item`，章节地址为 `/{slug}/ch/{number}`
- **章节阅读页**：`/{slug}/ch/{number}`
  - 封面/章节图片同样托管在 `https://bmcdn.my.id/...jpg`，从 HTML 中抽取。

### 参考实现

- 来源抽象接口：`data/source/ComicSource.kt`
- WZ 实现：`data/source/wz/WzSource.kt`
- AW 实现：`data/source/aw/AwSource.kt`

> **关于广告**：Wumboing 仅抓取漫画数据（标题、封面、章节、图片），**不会**携带原网站的任何广告内容，为读者提供干净、原生的阅读界面。

---

## 🏗️ 构建方法

### 环境要求

- JDK 17
- Android SDK（compileSdk 34、build-tools 34+）
- Android Gradle Plugin 8.5.2
- Kotlin 2.0.20

### 本地构建

```bash
# 生成调试版 APK
./gradlew assembleDebug

# 生成发布版 APK（R8 混淆 + 资源压缩）
./gradlew assembleRelease

# 生成发布版 AAB（Android App Bundle）
./gradlew bundleRelease

# 输出位置
# app/build/outputs/apk/debug/app-debug.apk
# app/build/outputs/apk/release/app-release.apk
# app/build/outputs/bundle/release/app-release.aab
```

> 本地构建需在 `local.properties` 中设置 `sdk.dir`，或设置环境变量 `ANDROID_HOME`。

---

## ⚙️ GitHub Actions

项目内置 GitHub Actions 工作流 **`.github/workflows/build.yml`**。

该工作流会在推送到 `main` 分支或手动触发（`workflow_dispatch`）时：

1. 检出代码
2. 安装 JDK 17（Temurin）
3. 配置 Android SDK
4. 执行 `./gradlew assembleRelease bundleRelease`
5. 上传生成的 APK 与 AAB 作为构建产物（Artifact）

构建产物可在仓库的 **Actions** 页面下载：
- `wumboing-apk`：发布版 APK
- `wumboing-aab`：发布版 AAB

---

## 📦 APK / AAB 信息

- 包名（applicationId）：`com.wumboing.app`
- 最低 Android 版本：API 24（Android 7.0）
- 目标 Android 版本：API 34
- 版本号：1（`versionCode` 1）
- 版本名：`1.0.0`
- 发布构建启用了 R8 混淆与资源压缩，以保持应用轻量。

---

## 🚀 安装方法

1. 从 **GitHub Actions** 产物中下载 `wumboing-apk`。
2. 将 `app-release.apk` 传输到 Android 设备。
3. 在设备上点击该 APK 文件进行安装。
   > 若提示“未知来源”，请在系统设置中允许安装未知来源的应用。
4. 打开 **Wumboing**，通过左上角汉堡菜单在 **WZ** 与 **AW** 之间切换。

---

## 💻 项目运行方法

1. 用 Android Studio 打开项目根目录（`settings.gradle.kts` 所在目录）。
2. 等待 Gradle 同步完成（首次会自动下载依赖）。
3. 连接设备或启动模拟器。
4. 点击 **Run** 运行应用。

命令行运行：

```bash
./gradlew installDebug
```

---

## 🔧 其他相关说明

- **应用 UI 语言**：印尼语（Bahasa Indonesia）
- **来源命名**：应用内仅显示 **WZ** 与 **AW**
- **阅读器**：竖屏滚动、支持全屏 / 非全屏切换
- **本地数据**：书签、阅读历史、主题偏好通过 DataStore 本地持久化
- **错误处理**：单个来源失败时显示印尼语错误提示，不影响另一来源
- **轻量设计**：避免多余依赖与资源，使用 R8 与资源压缩优化包体

---

## 📄 许可证

本项目基于 [LICENSE](LICENSE) 文件中包含的开源许可证发布。
