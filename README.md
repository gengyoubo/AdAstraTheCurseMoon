# Ad Astra: The Curse Moon

Minecraft 1.21.1 的 Architectury API 项目，目标平台为 NeoForge 和 Fabric，使用 Java 21 和 Mojang 官方映射。

构建工具固定为 Gradle 9.5.1、Architectury Loom 1.17.493、Architectury Plugin 3.5.170 和 Shadow 9.6.1。
部分前置以 Loom 1.17 构建，因此不能使用旧的 Loom 1.10 工具链。

## 代码结构

- `common/`：共享逻辑和资源，两个平台入口调用 `AdAstraTheCurseMoon.init()`。
- `neoforge/`：NeoForge 入口、`META-INF/neoforge.mods.toml` 和平台依赖。
- `fabric/`：Fabric 入口、`fabric.mod.json` 和平台依赖。

共享模块通过 Fabric 工件编译 Ad Astra 的共享 API；发布 JAR 仅合并本项目的共享代码，
不内置 Ad Astra、Architectury API 或 SSC。

## 构建与启动

将 IDEA 的 Gradle JVM 和命令行的 `JAVA_HOME` 设为 JDK 21，重新加载根项目的 Gradle 配置。

```powershell
.\gradlew.bat build
.\gradlew.bat :neoforge:runClient
.\gradlew.bat :fabric:runClient
```

服务端任务分别是 `:neoforge:runServer` 和 `:fabric:runServer`。
1.21.1 的运行目录分别为 `run/1.21.1/neoforge/` 和 `run/1.21.1/fabric/`。

发布 JAR：

- `neoforge/build/libs/ad_ssc-neoforge-1.21.1-1.0.0.jar`
- `fabric/build/libs/ad_ssc-fabric-1.21.1-1.0.0.jar`

安装游戏时使用不带 `dev`、`dev-shadow` 或 `sources` 后缀的 JAR，并另外安装对应平台的前置模组。

## 固定依赖

| 依赖 | NeoForge | Fabric |
| --- | --- | --- |
| 加载器 | NeoForge 21.1.249 | Fabric Loader 0.19.2 |
| Architectury API | 13.0.11 | 13.0.11 |
| Ad Astra | 1.16.26，Modrinth `9pnKHdCG` | 1.16.26，Modrinth `R1Zh2VaO` |
| SSC | 等待原生 NeoForge 前置，暂不加载或声明必需依赖 | Unofficial Port 1.10.0.25+1.21.1，CurseForge 文件 `8864475` |

两端使用 Resourceful Lib 3.0.12、Resourceful Config 3.0.11 和 Common Storage Lib 0.0.10。
1.21.1 的 Ad Astra 使用 Common Storage Lib，已移除旧的 Botarium 依赖。
Fabric 还配置 Fabric API、Pehkui、Satin、Player Animation Library 和 GeckoLib。
依赖坐标和版本集中在 `gradle.properties`，Gradle 开发启动任务会自动加载它们。

## SSC 的 Curse Maven

2026-10-17 已实际验证下列 POM 和 JAR 均可下载，即使 CurseForge 网页访问失败，也可以使用这个固定工件：

```groovy
modImplementation "curse.maven:shape-shifter-curse-unofficial-port-1542487:8864475"
```

- 项目：[Shape Shifter Curse-Unofficial Port](https://www.curseforge.com/minecraft/mc-mods/shape-shifter-curse-unofficial-port)
- 文件：[8864475](https://www.curseforge.com/minecraft/mc-mods/shape-shifter-curse-unofficial-port/files/8864475)
- 对应 Modrinth 版本：`maven.modrinth:shape-shifter-curse-unofficial-port:jKW8MgAY`

这个 Fabric 版的模组 ID 是 `shape-shifter-curse-unofficial`，Java 包名仍为
`net.onixary.shapeShifterCurseFabric`，与旧 1.20.1 版的模组 ID 不同。
NeoForge 端暂不接入 SSC，也不会自动改用 Connector 版本；待原生前置提供后再配置对应依赖和适配。
